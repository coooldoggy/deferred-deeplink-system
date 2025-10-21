# Android 샘플 앱

Deferred Deep Link SDK를 사용하는 Android 샘플 앱입니다.

## 📱 실행 방법

### 1. 서버 실행

먼저 백엔드 서버를 실행해야 합니다:

```bash
cd ../server
../gradlew bootRun
```

서버가 `http://localhost:8080`에서 실행됩니다.

### 2. 앱 실행

Android Studio에서:
1. `sample-android` 모듈 선택
2. Run 버튼 클릭 또는 `Shift + F10`

또는 터미널에서:
```bash
./gradlew :sample-android:installDebug
```

## 🧪 테스트 시나리오

### 시나리오 1: 매칭 성공 테스트

1. **링크 생성 (서버)**
   ```bash
   curl -X POST http://localhost:8080/api/v1/links \
     -H "Content-Type: application/json" \
     -d '{
       "targetUrl": "myapp://product/123",
       "campaignName": "Test Campaign",
       "customData": {"discount": "30"}
     }'
   ```
   
   응답에서 `linkId`를 복사합니다.

2. **링크 클릭 (브라우저)**
   - Chrome 브라우저에서 접속: `http://localhost:8080/d/{linkId}`
   - 또는 앱에서 "테스트 링크 열기" 버튼 클릭

3. **앱 재설치 또는 초기화**
   - 앱을 재설치하거나
   - 앱에서 "초기화 후 확인" 버튼 클릭

4. **결과 확인**
   - 매칭 성공 메시지 확인
   - Match Score 확인 (70% 이상이면 매칭)

### 시나리오 2: 실제 디바이스 테스트

1. **PC에서 링크 클릭**
   - PC 브라우저에서 `http://your-local-ip:8080/d/{linkId}` 접속
   - 서버가 PC의 디바이스 정보 저장

2. **모바일에서 앱 설치 및 실행**
   - 앱 설치 또는 "초기화 후 확인"
   - 같은 네트워크에 있어야 IP 매칭 가능

## 📋 주요 기능

### MainActivity

- **다시 확인**: 현재 상태에서 딥링크 확인
- **초기화 후 확인**: 저장된 상태를 초기화하고 다시 확인
- **테스트 링크 열기**: 브라우저로 테스트 링크 열기
- **로그 표시**: 실시간 로그 확인

### ProductActivity

딥링크로 이동하는 상품 화면 예제입니다.

## 🔧 설정

### 서버 URL 변경

`SampleApplication.kt`에서 서버 URL을 변경할 수 있습니다:

```kotlin
companion object {
    // 에뮬레이터용
    const val SERVER_URL = "http://10.0.2.2:8080"
    
    // 실제 디바이스용 (PC의 로컬 IP)
    // const val SERVER_URL = "http://192.168.1.100:8080"
    
    // 프로덕션
    // const val SERVER_URL = "https://your-domain.com"
}
```

### Deep Link 스킴 변경

`AndroidManifest.xml`에서 커스텀 스킴을 변경할 수 있습니다:

```xml
<data android:scheme="myapp" />
```

## 📝 참고사항

### 에뮬레이터에서 localhost 접근

- Android 에뮬레이터에서 호스트 머신의 localhost는 `10.0.2.2`입니다.
- 예: `http://10.0.2.2:8080`

### 실제 디바이스에서 테스트

1. PC와 디바이스가 같은 WiFi에 연결
2. PC의 로컬 IP 확인 (예: 192.168.1.100)
3. `SERVER_URL`을 로컬 IP로 변경
4. 방화벽에서 8080 포트 허용

### HTTP 통신 허용

테스트를 위해 `usesCleartextTraffic="true"`가 설정되어 있습니다.
프로덕션에서는 HTTPS를 사용하고 이 설정을 제거하세요.

## 🐛 트러블슈팅

### 네트워크 에러

- 서버가 실행 중인지 확인
- 인터넷 권한이 있는지 확인
- `usesCleartextTraffic="true"` 설정 확인

### 매칭 안됨

- 링크를 클릭한 후 24시간 이내에 테스트
- 같은 네트워크에서 테스트 (IP 매칭)
- 로그를 확인하여 디바이스 정보가 올바른지 확인

### 빌드 에러

```bash
# Gradle 캐시 정리
./gradlew clean

# 프로젝트 동기화
./gradlew :sample-android:dependencies
```

## 📸 스크린샷

앱을 실행하면:
1. **Header**: 앱 제목과 설명
2. **Result Card**: 매칭 결과 표시
3. **Buttons**: 다시 확인, 초기화, 테스트 링크
4. **Log**: 실시간 로그 표시

## 🔗 관련 링크

- [메인 README](../README.md)
- [구현 가이드](../IMPLEMENTATION_GUIDE.md)
- [서버 README](../server/README.md)

