# Deferred Deep Link 구현 가이드

이 문서는 Deferred Deep Link 시스템을 기존 프로젝트에 통합하는 방법을 설명합니다.

## 📋 Spring Server 이전 가이드

### 1. 파일 복사

다음 파일들을 기존 Spring 프로젝트로 복사하세요:

```
server/src/main/kotlin/com/deeplink/server/
├── domain/
│   ├── entity/
│   │   ├── DeepLink.kt
│   │   ├── DeviceFingerprint.kt
│   │   └── AttributionMatch.kt
│   └── dto/
│       └── DeepLinkDto.kt
├── repository/
│   ├── DeepLinkRepository.kt
│   ├── DeviceFingerprintRepository.kt
│   └── AttributionMatchRepository.kt
├── service/
│   └── DeepLinkService.kt
├── controller/
│   └── DeepLinkController.kt
├── config/
│   ├── DeepLinkProperties.kt
│   └── WebConfig.kt
└── util/
    └── FingerprintUtil.kt
```

### 2. 의존성 추가

`build.gradle.kts` 또는 `pom.xml`에 추가:

**Gradle:**
```kotlin
dependencies {
    // Oracle JDBC
    runtimeOnly("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")
    
    // UA Parser
    implementation("com.github.ua-parser:uap-java:1.5.4")
    
    // Redis (선택)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}
```

**Maven:**
```xml
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc11</artifactId>
    <version>23.3.0.23.09</version>
</dependency>
<dependency>
    <groupId>com.github.ua-parser</groupId>
    <artifactId>uap-java</artifactId>
    <version>1.5.4</version>
</dependency>
```

### 3. application.yml 설정 추가

```yaml
deeplink:
  matching-window-ms: 86400000  # 24시간
  link-expiry-ms: 2592000000    # 30일
  base-url: https://your-domain.com
```

### 4. 데이터베이스 테이블 생성

JPA `ddl-auto: update`를 사용하면 자동 생성되지만, 프로덕션에서는 수동 생성 권장:

```sql
-- DEEP_LINKS 테이블
CREATE TABLE DEEP_LINKS (
    id NUMBER PRIMARY KEY,
    link_id VARCHAR2(36) UNIQUE NOT NULL,
    target_url VARCHAR2(2000) NOT NULL,
    campaign_name VARCHAR2(500),
    campaign_source VARCHAR2(500),
    campaign_medium VARCHAR2(500),
    custom_data CLOB,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    click_count NUMBER DEFAULT 0,
    install_count NUMBER DEFAULT 0,
    active NUMBER(1) DEFAULT 1
);

CREATE SEQUENCE DEEPLINK_SEQ START WITH 1 INCREMENT BY 1;

-- DEVICE_FINGERPRINTS 테이블
CREATE TABLE DEVICE_FINGERPRINTS (
    id NUMBER PRIMARY KEY,
    link_id VARCHAR2(36) NOT NULL,
    fingerprint_hash VARCHAR2(64) NOT NULL,
    ip_address VARCHAR2(45) NOT NULL,
    user_agent CLOB NOT NULL,
    device_model VARCHAR2(100),
    os_name VARCHAR2(50),
    os_version VARCHAR2(50),
    browser_name VARCHAR2(100),
    browser_version VARCHAR2(50),
    language VARCHAR2(10),
    timezone VARCHAR2(50),
    screen_resolution VARCHAR2(50),
    created_at TIMESTAMP NOT NULL,
    matched NUMBER(1) DEFAULT 0
);

CREATE SEQUENCE FINGERPRINT_SEQ START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_fingerprint_hash ON DEVICE_FINGERPRINTS(fingerprint_hash);
CREATE INDEX idx_ip_address ON DEVICE_FINGERPRINTS(ip_address);
CREATE INDEX idx_created_at ON DEVICE_FINGERPRINTS(created_at);

-- ATTRIBUTION_MATCHES 테이블
CREATE TABLE ATTRIBUTION_MATCHES (
    id NUMBER PRIMARY KEY,
    link_id VARCHAR2(36) NOT NULL,
    device_id VARCHAR2(36) NOT NULL,
    fingerprint_id NUMBER NOT NULL,
    match_score NUMBER NOT NULL,
    matched_at TIMESTAMP NOT NULL,
    ip_address VARCHAR2(45),
    user_agent CLOB,
    custom_data CLOB
);

CREATE SEQUENCE ATTRIBUTION_SEQ START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_device_id ON ATTRIBUTION_MATCHES(device_id);
```

## 📱 SDK 사용 가이드

### Android 프로젝트 통합

#### 1. 라이브러리 추가

**로컬 모듈 사용:**
```kotlin
// settings.gradle.kts
include(":deeplink-sdk")
project(":deeplink-sdk").projectDir = file("../multiplatform-library-template/library")

// app/build.gradle.kts
dependencies {
    implementation(project(":deeplink-sdk"))
}
```

**또는 AAR 파일 사용:**
```bash
cd library
../gradlew assembleRelease
# build/outputs/aar/library-release.aar 생성
```

#### 2. AndroidManifest.xml

```xml
<manifest>
    <application
        android:name=".MyApplication">
        <!-- ... -->
    </application>
    
    <!-- 인터넷 권한 -->
    <uses-permission android:name="android.permission.INTERNET" />
</manifest>
```

#### 3. ProGuard 규칙 (릴리스 빌드)

```proguard
# DeepLink SDK
-keep class com.deeplink.sdk.** { *; }
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** { *; }
```

### iOS 프로젝트 통합

#### 1. Kotlin Multiplatform 프레임워크 생성

```bash
cd library
../gradlew linkDebugFrameworkIosSimulatorArm64  # 시뮬레이터
../gradlew linkReleaseFrameworkIosArm64         # 실제 디바이스
```

프레임워크 경로:
- Debug: `library/build/bin/iosSimulatorArm64/debugFramework/library.framework`
- Release: `library/build/bin/iosArm64/releaseFramework/library.framework`

#### 2. Xcode 프로젝트 설정

1. 프레임워크를 Xcode 프로젝트에 드래그
2. **General** → **Frameworks, Libraries, and Embedded Content** → **Embed & Sign** 선택
3. **Build Settings** → **Framework Search Paths**에 프레임워크 경로 추가

#### 3. Bridging Header (Objective-C 브리지 필요 시)

Kotlin/Native는 Objective-C 호환이므로 Swift에서 바로 사용 가능합니다.

```swift
import library  // 프레임워크 이름
```

## 🔄 워크플로우 예제

### 1. 마케팅 캠페인 시나리오

**백엔드 팀:**
```bash
# 캠페인 링크 생성
curl -X POST https://api.yourapp.com/api/v1/links \
  -H "Content-Type: application/json" \
  -d '{
    "targetUrl": "myapp://promo/summer2025",
    "campaignName": "Summer Sale 2025",
    "campaignSource": "facebook",
    "campaignMedium": "ad",
    "customData": {
      "discount": "30",
      "code": "SUMMER30"
    }
  }'

# 응답:
# {
#   "linkId": "abc123...",
#   "shortUrl": "https://api.yourapp.com/d/abc123..."
# }
```

**마케팅 팀:**
- Facebook 광고에 단축 URL 사용: `https://api.yourapp.com/d/abc123...`

**사용자 경험:**
1. Facebook에서 광고 클릭 (앱 미설치)
2. 앱스토어로 리다이렉트
3. 앱 설치 및 실행
4. Summer Sale 화면으로 자동 이동
5. 30% 할인 쿠폰 자동 적용

### 2. 친구 추천 시나리오

**앱 내 공유 기능:**

```kotlin
// Android
class ShareActivity : AppCompatActivity() {
    suspend fun generateReferralLink(userId: String): String {
        val response = apiClient.post("https://api.yourapp.com/api/v1/links") {
            setBody(CreateDeepLinkRequest(
                targetUrl = "myapp://referral/$userId",
                campaignName = "User Referral",
                customData = mapOf("referrerId" to userId)
            ))
        }
        return response.shortUrl
    }
    
    fun shareLink(link: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "앱 설치하고 혜택 받으세요! $link")
        }
        startActivity(Intent.createChooser(intent, "친구 초대"))
    }
}
```

**새 사용자 앱 실행:**

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        DeepLinkSDKHelper.checkDeferredDeepLink(this) { result ->
            if (result is DeepLinkResult.Success) {
                val referrerId = result.response.customData?.get("referrerId")
                
                // 추천인과 신규 가입자 모두에게 리워드 지급
                rewardReferral(referrerId, getCurrentUserId())
            }
        }
    }
}
```

## 🧪 테스트 체크리스트

### 서버 테스트

- [ ] 링크 생성 API 동작 확인
- [ ] 링크 클릭 시 리다이렉션 정상 작동
- [ ] 디바이스 핑거프린트 저장 확인
- [ ] 매칭 알고리즘 정확도 테스트
- [ ] 통계 API 정상 응답
- [ ] 만료된 링크 처리
- [ ] 동시성 테스트 (다수의 클릭/매칭)

### SDK 테스트

- [ ] Android 초기화 정상 작동
- [ ] iOS 초기화 정상 작동
- [ ] 디바이스 정보 수집 확인
- [ ] API 통신 성공
- [ ] 매칭 성공 케이스 테스트
- [ ] 매칭 실패 케이스 처리
- [ ] 네트워크 에러 처리
- [ ] 중복 호출 방지 (이미 확인된 경우)

### End-to-End 테스트

1. **정상 플로우:**
   - 링크 생성
   - 모바일 브라우저에서 클릭
   - 앱 설치 (또는 재설치)
   - 앱 실행 후 매칭 성공
   - 타겟 화면 이동 확인

2. **시간 차이 테스트:**
   - 링크 클릭 후 1시간 뒤 앱 실행 → 매칭 성공
   - 링크 클릭 후 24시간 뒤 앱 실행 → 매칭 여부 확인
   - 링크 클릭 후 25시간 뒤 앱 실행 → 매칭 실패

3. **다른 네트워크 테스트:**
   - WiFi에서 클릭, LTE에서 앱 실행 → 매칭 여부 확인

## 🔧 트러블슈팅

### 서버

**문제: 데이터베이스 연결 실패**
```
해결: application.yml의 데이터베이스 설정 확인
- URL, username, password 정확한지 확인
- Oracle 서비스가 실행 중인지 확인
- 방화벽 포트 오픈 확인
```

**문제: 매칭이 전혀 안됨**
```
해결:
1. 로그 확인: matching-window-ms 내에 클릭이 있었는지
2. IP 주소가 올바르게 추출되는지 확인
3. 디바이스 정보가 제대로 저장되는지 확인
```

### Android SDK

**문제: SDK 초기화 에러**
```
해결: Application 클래스에서 initialize() 호출 확인
- AndroidManifest.xml에 Application 클래스 등록 확인
```

**문제: 네트워크 에러**
```
해결:
1. AndroidManifest.xml에 INTERNET 권한 추가
2. cleartext traffic 허용 (테스트 시 HTTP 사용하는 경우)
```

### iOS SDK

**문제: 프레임워크 not found**
```
해결:
1. Framework Search Paths 확인
2. Embed & Sign 설정 확인
3. 프레임워크 재빌드
```

**문제: Swift에서 타입 인식 안됨**
```
해결:
1. import library 확인
2. Clean Build Folder (Cmd+Shift+K)
3. Derived Data 삭제
```

## 📊 모니터링 및 분석

### 주요 지표

1. **매칭률 (Match Rate)**
   ```sql
   SELECT 
       COUNT(DISTINCT m.device_id) * 100.0 / COUNT(DISTINCT f.id) as match_rate
   FROM DEVICE_FINGERPRINTS f
   LEFT JOIN ATTRIBUTION_MATCHES m ON f.id = m.fingerprint_id;
   ```

2. **전환율 (Conversion Rate)**
   ```sql
   SELECT 
       link_id,
       install_count * 100.0 / NULLIF(click_count, 0) as conversion_rate
   FROM DEEP_LINKS
   WHERE click_count > 0;
   ```

3. **평균 매칭 점수**
   ```sql
   SELECT AVG(match_score) as avg_match_score
   FROM ATTRIBUTION_MATCHES;
   ```

### 로깅

**권장 로그:**
- 링크 생성 (linkId, targetUrl)
- 클릭 추적 (linkId, IP, User-Agent)
- 매칭 시도 (deviceId, candidates count)
- 매칭 성공 (deviceId, linkId, score)
- 매칭 실패 (deviceId, reason)

**주의:** IP 주소, User-Agent 등은 개인정보이므로 로그 보관 정책에 따라 관리

## 🚀 프로덕션 체크리스트

### 서버

- [ ] HTTPS 적용
- [ ] Rate Limiting 설정
- [ ] 데이터베이스 인덱스 최적화
- [ ] 모니터링/알림 설정 (Prometheus, Grafana 등)
- [ ] 백업 정책 수립
- [ ] 로그 레벨 조정 (INFO 이상)
- [ ] 링크 생성 API 인증 추가
- [ ] CORS 설정 검토
- [ ] 응답 캐싱 (Redis)
- [ ] Health Check 엔드포인트 추가

### SDK

- [ ] 서버 URL 환경별 설정 (dev/staging/prod)
- [ ] ProGuard/R8 규칙 테스트
- [ ] 난독화 후 동작 확인
- [ ] 크래시 리포팅 통합 (Firebase Crashlytics 등)
- [ ] 사용자 개인정보 처리방침 업데이트
- [ ] 앱스토어 리뷰 가이드라인 준수 확인

## 📞 지원

구현 중 문제가 발생하면:
1. README.md의 API 문서 참고
2. 로그 확인
3. 테스트 체크리스트 수행
4. GitHub Issues에 문의

---

**Happy Coding! 🎉**

