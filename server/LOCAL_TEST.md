# 🚀 로컬 테스트 가이드 (DB 없이)

Oracle이나 Redis 없이 **H2 in-memory 데이터베이스**로 테스트할 수 있습니다!

## ✅ 서버 실행

### 방법 1: Gradle로 실행 (추천)

```bash
cd /Users/yulim/dev/workspace_android/multiplatform-library-template

# local 프로필로 실행
./gradlew :server:bootRun --args='--spring.profiles.active=local'
```

### 방법 2: JAR 빌드 후 실행

```bash
# 빌드
./gradlew :server:bootJar

# 실행
java -jar server/build/libs/server-1.0.0.jar --spring.profiles.active=local
```

### ✅ 서버 실행 확인

브라우저나 curl로 확인:
```bash
curl http://localhost:8080/api/v1/links
# 404 응답이 나오면 서버 정상 동작 중
```

## 🗄️ H2 데이터베이스 콘솔 (선택사항)

실시간으로 DB 데이터를 확인하고 싶다면:

1. **브라우저에서 접속:**
   ```
   http://localhost:8080/h2-console
   ```

2. **접속 정보:**
   ```
   JDBC URL: jdbc:h2:mem:testdb
   User Name: sa
   Password: (빈칸)
   ```

3. **Connect** 클릭

4. **테이블 확인:**
   ```sql
   SELECT * FROM DEEP_LINKS;
   SELECT * FROM DEVICE_FINGERPRINTS;
   SELECT * FROM ATTRIBUTION_MATCHES;
   ```

## 📝 전체 플로우 테스트

### 1단계: 서버 실행

```bash
./gradlew :server:bootRun --args='--spring.profiles.active=local'
```

### 2단계: 딥링크 생성

새 터미널에서:

```bash
curl -X POST http://localhost:8080/api/v1/links \
  -H "Content-Type: application/json" \
  -d '{
    "targetUrl": "coooldoggy://product/123",
    "campaignName": "Test Campaign",
    "campaignSource": "local-test",
    "customData": {
      "productId": "123",
      "discount": "50"
    }
  }'
```

**응답 예시:**
```json
{
  "linkId": "a1b2c3d4-...",
  "shortUrl": "http://localhost:8080/d/a1b2c3d4-...",
  "targetUrl": "coooldoggy://product/123",
  "createdAt": "2025-10-21T...",
  "expiresAt": "2025-11-20T..."
}
```

`linkId`를 복사해둡니다.

### 3단계: 링크 클릭 시뮬레이션

브라우저에서 접속:
```
http://localhost:8080/d/{linkId}
```

또는 curl로:
```bash
curl -L http://localhost:8080/d/{linkId}
```

서버 로그에서 디바이스 정보가 저장되는 것을 확인할 수 있습니다:
```
Tracked click for link: a1b2c3d4-..., fingerprint: abc123...
```

### 4단계: Android 샘플 앱 실행

#### SampleApplication.kt 확인

`sample-android/src/main/kotlin/com/deeplink/sample/SampleApplication.kt`에서 서버 URL이 올바른지 확인:

```kotlin
companion object {
    // 에뮬레이터용 (현재 설정)
    const val SERVER_URL = "http://10.0.2.2:8080"
    
    // 실제 디바이스용
    // const val SERVER_URL = "http://192.168.1.xxx:8080"
}
```

#### 앱 실행

1. Android Studio에서 `sample-android` 실행
2. 에뮬레이터 시작
3. 앱 자동으로 매칭 확인
4. "초기화 후 확인" 버튼 클릭

### 5단계: 매칭 결과 확인

앱 로그에서:
```
✅ 매칭 성공!
Link ID: a1b2c3d4-...
Target URL: coooldoggy://product/123
Match Score: 0.85
```

자동으로 ProductActivity로 이동합니다!

### 6단계: 통계 확인

```bash
curl http://localhost:8080/api/v1/links/{linkId}/stats
```

**응답:**
```json
{
  "linkId": "a1b2c3d4-...",
  "clickCount": 1,
  "installCount": 1,
  "conversionRate": 100.0
}
```

## 🎯 빠른 테스트 스크립트

모든 단계를 한 번에:

```bash
#!/bin/bash

# 서버 실행 (백그라운드)
./gradlew :server:bootRun --args='--spring.profiles.active=local' &
SERVER_PID=$!

# 서버 시작 대기
sleep 10

# 링크 생성
RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/links \
  -H "Content-Type: application/json" \
  -d '{
    "targetUrl": "coooldoggy://product/123",
    "campaignName": "Quick Test",
    "customData": {"discount": "30"}
  }')

LINK_ID=$(echo $RESPONSE | grep -o '"linkId":"[^"]*' | cut -d'"' -f4)
SHORT_URL=$(echo $RESPONSE | grep -o '"shortUrl":"[^"]*' | cut -d'"' -f4)

echo "✅ 딥링크 생성 완료!"
echo "Link ID: $LINK_ID"
echo "Short URL: $SHORT_URL"
echo ""
echo "다음 단계:"
echo "1. 브라우저에서 접속: $SHORT_URL"
echo "2. Android 샘플 앱 실행"
echo "3. '초기화 후 확인' 버튼 클릭"
echo ""
echo "서버 종료: kill $SERVER_PID"
```

## 🔧 설정 비교

### Local 프로필 (application-local.yml)
- ✅ H2 in-memory DB (설치 불필요)
- ✅ Redis 불필요
- ✅ 자동 테이블 생성
- ✅ H2 콘솔 활성화
- ✅ 상세 로깅

### 기본 프로필 (application.yml)
- ❌ Oracle DB 필요
- ❌ Redis 권장
- 프로덕션용

## 📊 로그 확인

서버 실행 시 다음과 같은 로그를 볼 수 있습니다:

```
Starting DeepLinkServerApplication using Java 17
The following 1 profile is active: "local"
H2 console available at '/h2-console'
Started DeepLinkServerApplication in 3.456 seconds
```

링크 생성 시:
```
Created deep link: a1b2c3d4-5678-90ab-cdef-1234567890ab
```

클릭 추적 시:
```
Tracked click for link: a1b2c3d4-..., fingerprint: abc123...
```

매칭 시도 시:
```
Device matched: device-id-123 -> a1b2c3d4-..., score: 0.85
```

## 💡 팁

### 빠른 재시작
```bash
# 서버 종료
Ctrl + C

# 바로 다시 시작
./gradlew :server:bootRun --args='--spring.profiles.active=local'
```

### 데이터 초기화
H2 in-memory DB는 서버 재시작하면 자동으로 초기화됩니다.

### 실제 디바이스에서 테스트
```bash
# Mac의 로컬 IP 확인
ifconfig | grep "inet " | grep -v 127.0.0.1

# 예: 192.168.1.100
# SampleApplication.kt에서:
# const val SERVER_URL = "http://192.168.1.100:8080"
```

### 여러 클라이언트 동시 테스트
서버는 한 번만 실행하고, 여러 앱/브라우저에서 동시에 테스트 가능합니다.

## 🐛 트러블슈팅

### 포트 이미 사용 중
```bash
# 8080 포트 사용 프로세스 확인
lsof -i :8080

# 종료
kill -9 <PID>
```

### 앱에서 연결 안됨
1. 서버가 실행 중인지 확인
2. 에뮬레이터: `10.0.2.2:8080` 사용
3. 실제 디바이스: Mac의 로컬 IP 사용
4. 방화벽 확인

### H2 콘솔 접속 안됨
- URL 확인: `http://localhost:8080/h2-console` (not h2console)
- JDBC URL: `jdbc:h2:mem:testdb` 정확히 입력

## 📚 추가 리소스

- [H2 Database 문서](http://www.h2database.com/)
- [Spring Profiles 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [메인 README](../README.md)

---

**이제 DB 없이 바로 테스트할 수 있습니다! 🎉**

