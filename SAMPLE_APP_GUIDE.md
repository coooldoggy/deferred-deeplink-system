# 샘플 앱 가이드

## 🎯 개요

이 프로젝트는 Deferred Deep Link 시스템의 전체 구현을 포함합니다:

1. **Spring Server** (`/server`) - 백엔드 API
2. **Kotlin Multiplatform SDK** (`/library`) - Android/iOS 라이브러리
3. **Android 샘플 앱** (`/sample-android`) - SDK 사용 예제

## 📱 Android 샘플 앱 실행

### 전제 조건

- Android Studio Hedgehog (2023.1.1) 이상
- JDK 11 이상
- Android SDK (API 24 이상)

### 단계별 실행 방법

#### 1단계: 서버 실행

터미널에서:

```bash
# 프로젝트 루트로 이동
cd /Users/yulim/dev/workspace_android/multiplatform-library-template

# 서버 실행
./gradlew :server:bootRun
```

서버가 `http://localhost:8080`에서 실행됩니다.

**서버 준비 확인:**
```bash
curl http://localhost:8080/api/v1/links
# 404 응답이 나와야 정상 (엔드포인트가 GET을 지원하지 않음)
```

#### 2단계: Android Studio에서 앱 실행

1. **Android Studio 열기**
   - 프로젝트 루트 폴더를 엽니다

2. **Gradle Sync**
   - File → Sync Project with Gradle Files
   - 또는 상단의 "Sync Now" 클릭

3. **Run Configuration 선택**
   - 상단 드롭다운에서 `sample-android` 선택
   - 에뮬레이터 또는 실제 디바이스 선택

4. **실행**
   - Run 버튼 (▶️) 클릭
   - 또는 `Shift + F10`

#### 3단계: 테스트

앱이 실행되면 자동으로 Deferred Deep Link 확인을 시도합니다.

## 🧪 전체 플로우 테스트

### 시나리오: 광고 클릭 → 앱 설치 → 프로모션 화면 진입

#### 1. 딥링크 생성

새 터미널 창에서:

```bash
curl -X POST http://localhost:8080/api/v1/links \
  -H "Content-Type: application/json" \
  -d '{
    "targetUrl": "myapp://product/summer-sale",
    "campaignName": "Summer Sale 2025",
    "campaignSource": "facebook",
    "campaignMedium": "ad",
    "customData": {
      "productId": "summer-sale",
      "discount": "50"
    }
  }'
```

**응답 예시:**
```json
{
  "linkId": "a1b2c3d4-5678-90ab-cdef-1234567890ab",
  "shortUrl": "http://localhost:8080/d/a1b2c3d4-5678-90ab-cdef-1234567890ab",
  "targetUrl": "myapp://product/summer-sale",
  "createdAt": "2025-10-21T...",
  "expiresAt": "2025-11-20T..."
}
```

`linkId`를 복사해둡니다.

#### 2. 브라우저에서 링크 클릭 (사용자가 광고를 클릭하는 상황 시뮬레이션)

**옵션 A: 에뮬레이터 사용 시**

에뮬레이터의 Chrome 브라우저에서:
```
http://10.0.2.2:8080/d/{linkId}
```

**옵션 B: 실제 디바이스 사용 시**

1. PC의 로컬 IP 확인:
   ```bash
   # macOS/Linux
   ifconfig | grep "inet "
   # 예: 192.168.1.100
   ```

2. 디바이스 브라우저에서:
   ```
   http://192.168.1.100:8080/d/{linkId}
   ```

링크를 클릭하면:
- 서버가 디바이스 정보를 저장합니다
- 앱스토어로 리다이렉트됩니다 (테스트에서는 404 페이지)

#### 3. 앱에서 매칭 확인

샘플 앱에서:
1. **"초기화 후 확인"** 버튼 클릭
2. 로그에서 매칭 진행 상황 확인
3. 매칭 성공 시:
   - ✅ 딥링크 매칭 성공 메시지
   - Match Score 표시 (70% 이상)
   - Custom Data 정보 표시

#### 4. 통계 확인

```bash
curl http://localhost:8080/api/v1/links/{linkId}/stats
```

**응답 예시:**
```json
{
  "linkId": "a1b2c3d4-5678-90ab-cdef-1234567890ab",
  "clickCount": 1,
  "installCount": 1,
  "conversionRate": 100.0
}
```

## 🎨 샘플 앱 UI 가이드

### 메인 화면 (MainActivity)

1. **헤더**
   - 앱 제목과 설명

2. **결과 카드** (매칭 후 표시)
   - 매칭 성공/실패 상태
   - Target URL, Campaign 정보
   - Match Score
   - "상품 화면으로 이동" 버튼

3. **액션 버튼들**
   - **다시 확인**: 현재 상태로 다시 매칭 시도
   - **초기화 후 확인**: SharedPreferences 초기화하고 처음부터 다시
   - **테스트 링크 열기**: 브라우저로 테스트 링크 열기

4. **로그 영역**
   - 실시간 로그 표시
   - 타임스탬프 포함
   - 성공/실패/정보 아이콘

### 상품 화면 (ProductActivity)

딥링크로 이동하는 예제 화면입니다.

## 🔍 디버깅

### 로그 확인

#### Android Logcat

```kotlin
// DeepLinkSDK에서 발생하는 로그 필터링
adb logcat | grep "DeepLink"
```

#### 서버 로그

서버 실행 터미널에서 확인:
- 링크 생성 로그
- 클릭 추적 로그
- 매칭 시도 로그
- 매칭 성공/실패 로그

### 네트워크 트래픽 확인

Android Studio → View → Tool Windows → App Inspection → Network Inspector

### 일반적인 문제

#### 1. "SDK not initialized" 에러

**원인:** Application 클래스에서 초기화 안됨

**해결:**
- `AndroidManifest.xml`에 `android:name=".SampleApplication"` 확인
- `SampleApplication.kt`의 `onCreate()`에서 `initialize()` 호출 확인

#### 2. 네트워크 에러

**원인:** 서버 URL 잘못됨 또는 서버 미실행

**해결:**
- 서버가 실행 중인지 확인
- 에뮬레이터: `10.0.2.2:8080` 사용
- 실제 디바이스: PC의 로컬 IP 사용
- 같은 WiFi 네트워크에 연결

#### 3. 매칭 안됨 (NoMatch)

**원인:** 
- 링크 클릭 안함
- 시간 초과 (24시간 이후)
- IP 주소 불일치

**해결:**
- 브라우저에서 링크 클릭 확인
- 같은 네트워크에서 테스트
- 로그에서 IP 주소 확인
- 클릭 후 24시간 이내 테스트

#### 4. Match Score가 낮음 (< 0.7)

**원인:** 디바이스 정보가 많이 다름

**해결:**
- 같은 디바이스/브라우저에서 클릭
- 같은 네트워크 사용 (IP 매칭)
- VPN 사용 시 해제

## 📊 데이터베이스 확인 (Oracle)

### 링크 조회

```sql
SELECT * FROM DEEP_LINKS ORDER BY CREATED_AT DESC;
```

### 클릭 기록 조회

```sql
SELECT * FROM DEVICE_FINGERPRINTS WHERE LINK_ID = 'your-link-id' ORDER BY CREATED_AT DESC;
```

### 매칭 기록 조회

```sql
SELECT 
    am.*,
    dl.CAMPAIGN_NAME,
    dl.TARGET_URL
FROM ATTRIBUTION_MATCHES am
JOIN DEEP_LINKS dl ON am.LINK_ID = dl.LINK_ID
ORDER BY am.MATCHED_AT DESC;
```

## 🚀 프로덕션 배포 준비

### 1. 서버 URL 변경

`sample-android/src/main/kotlin/com/deeplink/sample/SampleApplication.kt`:

```kotlin
companion object {
    const val SERVER_URL = "https://your-production-domain.com"
}
```

### 2. Cleartext Traffic 제거

`sample-android/src/main/AndroidManifest.xml`:

```xml
<!-- 제거 -->
android:usesCleartextTraffic="true"
```

### 3. ProGuard 적용

이미 `proguard-rules.pro`에 규칙이 설정되어 있습니다.

### 4. 서명 키 설정

`sample-android/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("your-keystore.jks")
            storePassword = "your-password"
            keyAlias = "your-alias"
            keyPassword = "your-password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

## 📚 다음 단계

1. **커스터마이징**
   - UI 디자인 변경
   - 딥링크 스킴 변경 (`myapp://` → `yourapp://`)
   - 타겟 화면 추가

2. **기능 추가**
   - 푸시 알림 통합
   - Analytics 연동
   - A/B 테스팅

3. **iOS 앱 개발**
   - Xcode 프로젝트 생성
   - KMP 프레임워크 통합
   - iOS 예제 코드 참고: `library/src/iosMain/kotlin/com/deeplink/sdk/example/`

## 💡 팁

### 빠른 테스트 사이클

1. 서버 한 번 실행 후 계속 실행 상태 유지
2. 앱에서 "초기화 후 확인" 버튼 사용
3. 같은 링크로 반복 테스트 가능

### 로그 활성화

```kotlin
// 향후 추가 예정
DeepLinkSDKBuilder()
    .serverUrl(SERVER_URL)
    .enableLogging(true)  // 상세 로그 활성화
    .build()
```

### 커스텀 핑거프린팅

서버의 `FingerprintUtil.kt`에서 매칭 알고리즘 조정 가능:
- 가중치 조정
- 매칭 임계값 변경
- 추가 속성 추가

## 🤝 도움이 필요하신가요?

- [메인 README](./README.md)
- [구현 가이드](./IMPLEMENTATION_GUIDE.md)
- [샘플 앱 README](./sample-android/README.md)
- GitHub Issues

---

**즐거운 코딩 되세요! 🎉**

