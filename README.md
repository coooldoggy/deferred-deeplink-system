# Deferred Deep Link System

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-blue.svg)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green.svg)](https://spring.io/projects/spring-boot)

앱 설치 이전에 클릭한 링크를 추적하고, 설치 후 첫 실행 시 해당 링크로 사용자를 안내하는 Deferred Deep Link 시스템입니다.

**🔗 GitHub Repository**: https://github.com/coooldoggy/deferred-deeplink-system

## 📋 목차

- [개요](#개요)
- [아키텍처](#아키텍처)
- [구성 요소](#구성-요소)
- [샘플 앱](#샘플-앱)
- [빠른 시작](#빠른-시작)
- [API 문서](#api-문서)
- [사용 예제](#사용-예제)
- [배포](#배포)

## 🎯 개요

### Deferred Deep Link란?

일반 Deep Link와 달리, **앱이 설치되지 않은 상태**에서도 링크를 추적할 수 있는 기술입니다.

**동작 흐름:**
1. 사용자가 링크 클릭 (앱 미설치)
2. 서버가 디바이스 정보 저장 (IP, User-Agent 등)
3. 앱스토어로 리다이렉트
4. 앱 설치 및 첫 실행
5. SDK가 디바이스 정보를 서버로 전송
6. 서버가 확률적 매칭으로 원래 링크 찾기
7. 앱이 해당 화면으로 이동

### 매칭 방식

**디바이스 핑거프린팅 + 확률적 매칭**
- IP 주소 (40%)
- OS 정보 (20%)
- 디바이스 모델 (15%)
- Language (10%)
- Timezone (10%)
- Screen Resolution (5%)
- 시간 차이 패널티

매칭 임계값: **70% 이상**

## 🏗️ 아키텍처

```
┌─────────────────┐
│  Spring Server  │
│  (Backend API)  │
└────────┬────────┘
         │
         ├─ REST API
         ├─ Device Fingerprinting
         ├─ Attribution Matching
         └─ Oracle Database
         
┌──────────────────────────────┐
│ Kotlin Multiplatform SDK     │
│  ├─ Common (API Client)      │
│  ├─ Android (Device Info)    │
│  └─ iOS (Device Info)        │
└──────────────────────────────┘
```

## 📦 구성 요소

### 1. Spring Server (`/server`)

**기술 스택:**
- Spring Boot 3.2.0
- Kotlin
- Oracle Database
- Redis (선택)
- JPA/Hibernate

**주요 기능:**
- 딥링크 생성 및 관리
- 디바이스 핑거프린트 수집 및 저장
- 확률적 디바이스 매칭
- 통계 및 분석

### 2. Kotlin Multiplatform SDK (`/library`)

**지원 플랫폼:**
- Android
- iOS
- JVM (서버 테스트용)

**주요 기능:**
- 디바이스 정보 수집
- API 통신
- 자동 매칭 처리

### 3. Android 샘플 앱 (`/sample-android`)

**주요 기능:**
- SDK 사용 예제
- 실시간 로그 표시
- 매칭 결과 시각화
- 테스트 도구

### 4. iOS 샘플 앱 (`/sample-ios`)

**주요 기능:**
- SwiftUI 기반
- SDK 사용 예제
- 실시간 로그 표시
- 매칭 결과 시각화

## 📱 샘플 앱

동작하는 샘플 앱이 포함되어 있습니다!

### Android 샘플 앱

#### 빠른 실행

```bash
# 1. 서버 실행
./gradlew :server:bootRun

# 2. Android Studio에서 sample-android 모듈 실행
```

**상세 가이드:** 
- [Android 샘플 앱 README](./sample-android/README.md)
- [전체 가이드](./SAMPLE_APP_GUIDE.md)

### iOS 샘플 앱

#### 빠른 실행

```bash
# 1. 서버 실행
./gradlew :server:bootRun

# 2. KMP 프레임워크 빌드
./gradlew :library:linkDebugFrameworkIosSimulatorArm64

# 3. Xcode에서 프로젝트 생성 및 실행
```

**상세 가이드:**
- [iOS 샘플 앱 README](./sample-ios/README.md)
- [빠른 설정 가이드](./sample-ios/SETUP_GUIDE.md)

### 주요 기능

- ✅ **자동 매칭**: 앱 시작 시 자동으로 딥링크 확인
- ✅ **실시간 로그**: 매칭 과정을 로그로 확인
- ✅ **테스트 도구**: 브라우저 링크 열기, 초기화 등
- ✅ **결과 시각화**: 매칭 점수, 캠페인 정보 표시
- ✅ **Android + iOS**: 양쪽 플랫폼 모두 지원

### 화면 구성

샘플 앱은 다음 기능을 제공합니다:

1. **메인 화면**
   - Deferred Deep Link 자동 확인
   - 매칭 결과 표시 (성공/실패)
   - Match Score 시각화
   - 실시간 로그 출력

2. **테스트 도구**
   - "다시 확인" - 현재 상태에서 재시도
   - "초기화 후 확인" - 완전히 처음부터 다시
   - "테스트 링크 열기" - 브라우저로 테스트

3. **상품 화면**
   - 딥링크로 이동하는 예제 화면
   - 커스텀 데이터 표시

## 🚀 빠른 시작

### 🎯 로컬 테스트 (DB 없이) - 추천!

**가장 빠른 방법:** Oracle/Redis 없이 H2 in-memory DB로 바로 테스트

```bash
# 서버 실행
./gradlew :server:bootRun --args='--spring.profiles.active=local'

# 새 터미널에서 테스트
curl -X POST http://localhost:8080/api/v1/links \
  -H "Content-Type: application/json" \
  -d '{"targetUrl":"coooldoggy://product/123","campaignName":"Test"}'

# Android 샘플 앱 실행 (Android Studio)
# - sample-android 모듈 선택
# - Run 버튼 클릭
```

**상세 가이드:** [server/LOCAL_TEST.md](./server/LOCAL_TEST.md)

---

### 1. 프로덕션 서버 설정

#### Oracle 데이터베이스 준비

```sql
-- 데이터베이스 사용자 생성
CREATE USER deeplink IDENTIFIED BY deeplink;
GRANT CONNECT, RESOURCE TO deeplink;
GRANT UNLIMITED TABLESPACE TO deeplink;

-- 시퀀스는 자동 생성됩니다 (JPA)
```

#### application.yml 수정

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:XE
    username: deeplink
    password: deeplink
  
  data:
    redis:
      host: localhost
      port: 6379

deeplink:
  base-url: https://your-domain.com
  matching-window-ms: 86400000  # 24시간
```

#### 서버 실행

```bash
cd server
../gradlew bootRun
```

서버가 `http://localhost:8080`에서 실행됩니다.

### 2. Android 설정

#### build.gradle.kts

```kotlin
dependencies {
    implementation(project(":library"))
    // 또는
    // implementation("io.github.yourusername:deeplink-sdk:1.0.0")
}
```

#### Application 클래스

```kotlin
import android.app.Application
import com.deeplink.sdk.DeepLinkSDKHelper

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        DeepLinkSDKHelper.initialize(
            application = this,
            serverUrl = "https://your-server.com"
        )
    }
}
```

#### MainActivity

```kotlin
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.deeplink.sdk.DeepLinkSDKHelper
import com.deeplink.sdk.models.DeepLinkResult

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Deferred Deep Link 확인
        DeepLinkSDKHelper.checkDeferredDeepLink(this) { result ->
            when (result) {
                is DeepLinkResult.Success -> {
                    val response = result.response
                    
                    // 타겟 URL로 이동
                    navigateToTarget(response.targetUrl)
                }
                
                is DeepLinkResult.NoMatch -> {
                    // 일반 설치, 홈 화면 표시
                }
                
                is DeepLinkResult.Error -> {
                    // 에러 처리
                }
            }
        }
    }
}
```

### 3. iOS 설정

#### AppDelegate.swift

```swift
import UIKit
import SharedLibrary // KMP 프레임워크

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        // DeepLink SDK 초기화
        DeepLinkSDKHelper.shared.initialize(
            serverUrl: "https://your-server.com"
        )
        return true
    }
}
```

#### ViewController.swift

```swift
import UIKit
import SharedLibrary

class ViewController: UIViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Deferred Deep Link 확인
        DeepLinkSDKHelper.shared.checkDeferredDeepLink { result in
            switch result {
            case let success as DeepLinkResult.Success:
                let response = success.response
                // 타겟 URL로 이동
                self.navigateToTarget(response.targetUrl)
                
            case is DeepLinkResult.NoMatch:
                // 일반 설치, 홈 화면 표시
                break
                
            case let error as DeepLinkResult.Error:
                // 에러 처리
                print("Error: \(error.message)")
                
            default:
                break
            }
        }
    }
}
```

## 📚 API 문서

### 서버 API

#### 1. 딥링크 생성

```http
POST /api/v1/links
Content-Type: application/json

{
  "targetUrl": "myapp://product/123",
  "campaignName": "summer_sale",
  "campaignSource": "facebook",
  "campaignMedium": "social",
  "customData": {
    "productId": "123",
    "discount": "20"
  },
  "expiryDays": 30
}
```

**응답:**

```json
{
  "linkId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "shortUrl": "https://your-domain.com/d/a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "targetUrl": "myapp://product/123",
  "createdAt": "2025-10-21T12:34:56",
  "expiresAt": "2025-11-20T12:34:56"
}
```

#### 2. 링크 클릭 (리다이렉션)

```http
GET /d/{linkId}
```

자동으로:
1. 디바이스 정보 수집 및 저장
2. 클릭 카운트 증가
3. 앱스토어로 리다이렉트

#### 3. 디바이스 매칭

```http
POST /api/v1/match
Content-Type: application/json

{
  "deviceId": "unique-device-id",
  "userAgent": "...",
  "deviceModel": "Samsung Galaxy S21",
  "osName": "Android",
  "osVersion": "13",
  "language": "ko_KR",
  "timezone": "Asia/Seoul",
  "screenResolution": "1080x2400",
  "timestamp": 1697878400000
}
```

**응답 (매칭 성공):**

```json
{
  "matched": true,
  "linkId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "targetUrl": "myapp://product/123",
  "campaignName": "summer_sale",
  "customData": {
    "productId": "123",
    "discount": "20"
  },
  "matchScore": 0.85
}
```

**응답 (매칭 실패):**

```json
{
  "matched": false
}
```

#### 4. 통계 조회

```http
GET /api/v1/links/{linkId}/stats
```

**응답:**

```json
{
  "linkId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "clickCount": 150,
  "installCount": 45,
  "conversionRate": 30.0
}
```

### SDK API

#### Android

```kotlin
// 초기화
DeepLinkSDKHelper.initialize(application, serverUrl)

// 매칭 확인 (콜백)
DeepLinkSDKHelper.checkDeferredDeepLink(context) { result ->
    // 처리
}

// 매칭 확인 (Coroutine)
val result = DeepLinkSDKHelper.checkDeferredDeepLinkSuspend(context)

// 테스트용 리셋
DeepLinkSDKHelper.resetDeferredDeepLinkCheck(context)
```

#### iOS

```swift
// 초기화
DeepLinkSDKHelper.shared.initialize(serverUrl: serverUrl)

// 매칭 확인 (콜백)
DeepLinkSDKHelper.shared.checkDeferredDeepLink { result in
    // 처리
}

// 매칭 확인 (Async/Await)
let result = await DeepLinkSDKHelper.shared.checkDeferredDeepLinkSuspend()

// 테스트용 리셋
DeepLinkSDKHelper.shared.resetDeferredDeepLinkCheck()
```

## 💡 사용 예제

### 마케팅 캠페인 링크 생성

```bash
curl -X POST https://your-server.com/api/v1/links \
  -H "Content-Type: application/json" \
  -d '{
    "targetUrl": "myapp://promotion/blackfriday",
    "campaignName": "Black Friday 2025",
    "campaignSource": "instagram",
    "campaignMedium": "story",
    "customData": {
      "promoCode": "BF2025",
      "discount": "50"
    }
  }'
```

### 앱에서 프로모션 처리

```kotlin
// Android
DeepLinkSDKHelper.checkDeferredDeepLink(this) { result ->
    when (result) {
        is DeepLinkResult.Success -> {
            val customData = result.response.customData
            val promoCode = customData?.get("promoCode")
            val discount = customData?.get("discount")
            
            // 프로모션 화면으로 이동
            startActivity(Intent(this, PromotionActivity::class.java).apply {
                putExtra("promo_code", promoCode)
                putExtra("discount", discount)
            })
        }
        else -> {
            // 홈 화면
        }
    }
}
```

## 🔧 고급 설정

### 매칭 윈도우 변경

```yaml
# application.yml
deeplink:
  matching-window-ms: 172800000  # 48시간
```

### Redis 캐싱 활성화

Redis를 사용하면 매칭 성능이 향상됩니다.

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: your-password  # 선택
```

### 커스텀 앱스토어 URL

```kotlin
// DeepLinkRedirectController.kt 수정
private fun getAppStoreUrl(request: HttpServletRequest): String {
    val userAgent = request.getHeader("User-Agent")?.lowercase() ?: ""
    
    return when {
        userAgent.contains("android") -> {
            "https://play.google.com/store/apps/details?id=com.yourapp"
        }
        userAgent.contains("iphone") || userAgent.contains("ipad") -> {
            "https://apps.apple.com/app/idYOUR_APP_ID"
        }
        else -> {
            "https://your-landing-page.com"
        }
    }
}
```

## 📊 데이터베이스 스키마

### DEEP_LINKS

| Column | Type | Description |
|--------|------|-------------|
| id | NUMBER | Primary Key |
| link_id | VARCHAR2(36) | 고유 링크 ID (UUID) |
| target_url | VARCHAR2(2000) | 타겟 URL |
| campaign_name | VARCHAR2(500) | 캠페인 이름 |
| created_at | TIMESTAMP | 생성 시간 |
| click_count | NUMBER | 클릭 수 |
| install_count | NUMBER | 설치 수 |

### DEVICE_FINGERPRINTS

| Column | Type | Description |
|--------|------|-------------|
| id | NUMBER | Primary Key |
| link_id | VARCHAR2(36) | 딥링크 ID |
| fingerprint_hash | VARCHAR2(64) | 핑거프린트 해시 |
| ip_address | VARCHAR2(45) | IP 주소 |
| user_agent | CLOB | User Agent |
| created_at | TIMESTAMP | 생성 시간 |
| matched | NUMBER(1) | 매칭 여부 |

### ATTRIBUTION_MATCHES

| Column | Type | Description |
|--------|------|-------------|
| id | NUMBER | Primary Key |
| link_id | VARCHAR2(36) | 딥링크 ID |
| device_id | VARCHAR2(36) | 디바이스 ID |
| fingerprint_id | NUMBER | 핑거프린트 ID |
| match_score | NUMBER | 매칭 점수 |
| matched_at | TIMESTAMP | 매칭 시간 |

## 🚢 배포

### 서버 배포

```bash
# JAR 빌드
cd server
../gradlew bootJar

# 실행
java -jar build/libs/server-1.0.0.jar \
  --spring.datasource.url=jdbc:oracle:thin:@your-db:1521:PROD \
  --spring.datasource.username=deeplink \
  --spring.datasource.password=secure-password \
  --deeplink.base-url=https://your-domain.com
```

### SDK 배포 (Maven Central)

```bash
# 라이브러리 퍼블리시
cd library
../gradlew publish
```

## 🧪 테스트

### 서버 테스트

```bash
cd server
../gradlew test
```

### SDK 테스트

```bash
# Android
./gradlew :library:androidHostTest

# iOS
./gradlew :library:iosSimulatorArm64Test
```

### 수동 테스트 시나리오

1. **링크 생성**
   ```bash
   curl -X POST http://localhost:8080/api/v1/links -H "Content-Type: application/json" -d '{"targetUrl":"myapp://test"}'
   ```

2. **브라우저에서 링크 클릭**
   ```
   http://localhost:8080/d/{linkId}
   ```

3. **앱 실행 및 SDK 호출**
   - 앱에서 `checkDeferredDeepLink()` 호출
   - 매칭 결과 확인

4. **통계 확인**
   ```bash
   curl http://localhost:8080/api/v1/links/{linkId}/stats
   ```

## 📝 라이선스

이 프로젝트는 샘플/템플릿 용도로 제공됩니다.

## 🤝 기여

이슈와 PR은 언제나 환영합니다!

## 📧 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.

---

**Note:** 이 코드는 프로덕션 환경에서 사용하기 전에 추가적인 보안 검토와 테스트가 필요합니다.

## 🔐 보안 고려사항

1. **HTTPS 사용**: 프로덕션에서는 반드시 HTTPS 사용
2. **Rate Limiting**: API 엔드포인트에 rate limiting 적용
3. **데이터베이스 암호화**: 민감한 정보 암호화
4. **로깅**: 개인정보는 로그에 남기지 않기
5. **인증/인가**: 링크 생성 API에 인증 추가 권장

## 🎯 향후 개선사항

- [ ] IDFA/GAID 추가 지원 (옵트인 시)
- [ ] 클립보드 기반 매칭 추가
- [ ] 웹 대시보드 구축
- [ ] 실시간 통계 (WebSocket)
- [ ] A/B 테스팅 지원
- [ ] 딥링크 QR 코드 생성
