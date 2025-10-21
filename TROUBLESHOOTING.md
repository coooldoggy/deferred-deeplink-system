# 🔧 트러블슈팅 가이드

## ❌ Android 앱에서 Network Error

### 체크리스트

#### 1️⃣ 서버가 실행 중인지 확인

터미널에서:
```bash
curl http://localhost:8080/api/v1/links
```

**예상 응답:**
```json
{"timestamp":"...","status":405,"error":"Method Not Allowed"...}
```

✅ 이 응답이 나오면 서버 정상
❌ "Connection refused" 또는 응답 없음 → 서버 실행 필요

**서버 실행:**
```bash
./gradlew :server:bootRun --args='--spring.profiles.active=local'
```

#### 2️⃣ 에뮬레이터 vs 실제 디바이스

**에뮬레이터 사용 시:**
```kotlin
// SampleApplication.kt
const val SERVER_URL = "http://10.0.2.2:8080"  // ✅ 올바름
```

**실제 디바이스 사용 시:**
```kotlin
// 1. Mac의 로컬 IP 확인
// 터미널: ifconfig | grep "inet " | grep -v 127.0.0.1
// 예: 192.168.1.100

// 2. SampleApplication.kt 수정
const val SERVER_URL = "http://192.168.1.100:8080"

// 3. Mac과 디바이스가 같은 WiFi에 연결되어 있어야 함
```

#### 3️⃣ AndroidManifest.xml 확인

다음 설정이 있는지 확인:

```xml
<!-- 인터넷 권한 -->
<uses-permission android:name="android.permission.INTERNET" />

<application
    ...
    android:usesCleartextTraffic="true">  <!-- HTTP 허용 -->
</application>
```

#### 4️⃣ 방화벽 확인 (실제 디바이스 사용 시)

Mac에서:
```bash
# 시스템 환경설정 → 보안 및 개인 정보 보호 → 방화벽
# 또는 8080 포트 허용:
sudo pfctl -d  # 방화벽 임시 비활성화 (테스트용)
```

#### 5️⃣ Logcat으로 정확한 에러 확인

Android Studio에서:
1. **Logcat** 탭 열기
2. 필터 설정: `DeepLink` 또는 `okhttp`
3. 에러 메시지 확인

예상되는 로그:
```
✅ 정상: I/DeepLinkSDK: Checking deferred deep link...
❌ 에러: E/okhttp: java.net.ConnectException: Failed to connect to /10.0.2.2:8080
```

### 해결 방법

#### A. 서버 미실행

**증상:**
```
java.net.ConnectException: Connection refused
```

**해결:**
```bash
./gradlew :server:bootRun --args='--spring.profiles.active=local'
```

#### B. 잘못된 URL

**증상:**
```
java.net.UnknownHostException
```

**해결:**
- 에뮬레이터: `http://10.0.2.2:8080`
- 실제 디바이스: `http://192.168.x.x:8080` (Mac IP)

#### C. Cleartext HTTP 차단

**증상:**
```
java.net.UnknownServiceException: CLEARTEXT communication not permitted
```

**해결:**
`AndroidManifest.xml`에 추가:
```xml
android:usesCleartextTraffic="true"
```

#### D. 권한 없음

**증상:**
```
java.lang.SecurityException: Permission denied
```

**해결:**
`AndroidManifest.xml`에 추가:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 🧪 테스트 방법

### 1. 서버 테스트

```bash
# 서버 실행 확인
curl http://localhost:8080/api/v1/links

# 링크 생성 테스트
curl -X POST http://localhost:8080/api/v1/links \
  -H "Content-Type: application/json" \
  -d '{"targetUrl":"coooldoggy://test","campaignName":"Test"}'
```

### 2. 에뮬레이터에서 서버 접근 테스트

에뮬레이터의 Chrome 브라우저에서:
```
http://10.0.2.2:8080/api/v1/links
```

405 에러가 나오면 정상!

### 3. 앱 로그 확인

`MainActivity.kt`에서 에러 로그 추가:

```kotlin
DeepLinkSDKHelper.checkDeferredDeepLink(this) { result ->
    when (result) {
        is DeepLinkResult.Error -> {
            addLog("❌ 에러: ${result.message}", isError = true)
            result.exception?.let {
                addLog("상세: ${it.message}", isError = true)
                it.printStackTrace()  // Logcat에 스택트레이스 출력
            }
        }
        ...
    }
}
```

## 🔍 상세 디버깅

### Logcat 필터

```
# DeepLink 관련 로그만
tag:DeepLink

# 네트워크 에러
tag:okhttp level:error

# 전체 앱 로그
package:com.coooldoggy.deeplink.sample
```

### 네트워크 Inspector

Android Studio → View → Tool Windows → App Inspection → Network Inspector

여기서 실제 HTTP 요청/응답을 볼 수 있습니다.

### Ktor Client 로그 활성화

`DeepLinkSDK.kt`에 로깅 추가:

```kotlin
private val client = HttpClient {
    install(ContentNegotiation) {
        json(...)
    }
    install(Logging) {  // 추가
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
}
```

의존성 추가 필요:
```kotlin
implementation("io.ktor:ktor-client-logging:2.3.7")
```

## 🛠️ 빠른 해결 체크

### 단계별 확인

1. **서버 확인**
   ```bash
   curl http://localhost:8080/api/v1/links
   # 405 응답 나와야 함
   ```

2. **에뮬레이터에서 브라우저로 확인**
   ```
   http://10.0.2.2:8080/api/v1/links
   ```

3. **앱 로그 확인**
   - Logcat에서 에러 메시지 확인

4. **URL 확인**
   - 에뮬레이터: 10.0.2.2
   - 실제 디바이스: Mac의 로컬 IP

5. **권한 확인**
   - INTERNET 권한
   - usesCleartextTraffic

## 💡 일반적인 해결책

### 즉시 시도할 것

```bash
# 1. 서버 재시작
pkill -f "server:bootRun"
./gradlew :server:bootRun --args='--spring.profiles.active=local'

# 2. 앱 재설치
./gradlew :sample-android:uninstallDebug
./gradlew :sample-android:installDebug

# 3. 에뮬레이터 재시작
```

### 실제 디바이스에서 테스트하는 경우

1. **Mac IP 확인**
   ```bash
   ifconfig en0 | grep "inet " | awk '{print $2}'
   ```

2. **SampleApplication.kt 수정**
   ```kotlin
   const val SERVER_URL = "http://192.168.1.xxx:8080"
   ```

3. **Mac과 디바이스가 같은 WiFi 연결 확인**

4. **재빌드 및 설치**
   ```bash
   ./gradlew :sample-android:installDebug
   ```

## 📝 로그 예시

### 정상 동작 시

```
I/DeepLinkSDK: Initializing with server: http://10.0.2.2:8080
I/DeepLinkSDK: Checking deferred deep link for device: xxx
I/okhttp: --> POST http://10.0.2.2:8080/api/v1/match
I/okhttp: <-- 200 OK (123ms)
I/DeepLinkSDK: Device matched! Score: 0.85
```

### 에러 발생 시

```
E/DeepLinkSDK: Failed to check deferred deep link
E/okhttp: java.net.ConnectException: Failed to connect to /10.0.2.2:8080
    at okhttp3.internal.connection.RealConnection.connectSocket(...)
```

## 🆘 여전히 안되면

1. **서버 로그 확인**
   - 요청이 서버에 도달하는지 확인
   - 서버 터미널에서 로그 확인

2. **프록시/VPN 확인**
   - VPN 비활성화
   - 프록시 설정 제거

3. **Android Studio 재시작**

4. **Gradle Sync**
   - File → Sync Project with Gradle Files

5. **GitHub Issue 생성**
   - 로그 첨부
   - https://github.com/coooldoggy/deferred-deeplink-system/issues

---

**대부분의 경우 서버 실행 + 올바른 URL 설정으로 해결됩니다!**

