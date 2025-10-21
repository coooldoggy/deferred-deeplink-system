# 프로젝트 구조

```
multiplatform-library-template/
├── 📂 server/                          # Spring Boot 서버
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── kotlin/com/deeplink/server/
│   │   │   ├── DeepLinkServerApplication.kt
│   │   │   ├── config/
│   │   │   │   ├── DeepLinkProperties.kt
│   │   │   │   └── WebConfig.kt
│   │   │   ├── controller/
│   │   │   │   └── DeepLinkController.kt
│   │   │   ├── domain/
│   │   │   │   ├── dto/
│   │   │   │   │   └── DeepLinkDto.kt
│   │   │   │   └── entity/
│   │   │   │       ├── AttributionMatch.kt
│   │   │   │       ├── DeepLink.kt
│   │   │   │       └── DeviceFingerprint.kt
│   │   │   ├── repository/
│   │   │   │   ├── AttributionMatchRepository.kt
│   │   │   │   ├── DeepLinkRepository.kt
│   │   │   │   └── DeviceFingerprintRepository.kt
│   │   │   ├── service/
│   │   │   │   └── DeepLinkService.kt
│   │   │   └── util/
│   │   │       └── FingerprintUtil.kt
│   │   └── resources/
│   │       └── application.yml
│   └── README.md
│
├── 📂 library/                         # Kotlin Multiplatform SDK
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/com/deeplink/sdk/
│       │   ├── DeepLinkSDK.kt
│       │   ├── DeepLinkConfig.kt
│       │   └── models/
│       │       └── DeepLinkModels.kt
│       ├── androidMain/kotlin/com/deeplink/sdk/
│       │   ├── DeviceInfoProvider.android.kt
│       │   ├── DeepLinkSDKHelper.android.kt
│       │   └── example/
│       │       └── ExampleUsage.android.kt
│       └── iosMain/kotlin/com/deeplink/sdk/
│           ├── DeviceInfoProvider.ios.kt
│           ├── DeepLinkSDKHelper.ios.kt
│           └── example/
│               └── ExampleUsage.ios.kt
│
├── 📂 sample-android/                  # Android 샘플 앱
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── kotlin/com/deeplink/sample/
│   │   │   ├── SampleApplication.kt
│   │   │   ├── MainActivity.kt
│   │   │   └── ProductActivity.kt
│   │   └── res/
│   │       ├── layout/
│   │       │   ├── activity_main.xml
│   │       │   └── activity_product.xml
│   │       └── values/
│   │           ├── strings.xml
│   │           ├── colors.xml
│   │           └── themes.xml
│   └── README.md
│
├── 📂 sample-ios/                      # iOS 샘플 앱 (Swift)
│   ├── SampleApp/
│   │   ├── SampleApp.swift            # 앱 진입점
│   │   ├── ContentView.swift          # 메인 화면 (SwiftUI)
│   │   ├── DeepLinkViewModel.swift    # 뷰모델
│   │   └── ProductView.swift          # 상품 화면
│   ├── Info.plist.example             # Info.plist 예제
│   ├── SETUP_GUIDE.md                 # 빠른 설정 가이드
│   └── README.md                      # 상세 가이드
│
├── 📄 build.gradle.kts                 # 루트 빌드 스크립트
├── 📄 settings.gradle.kts              # Gradle 설정
├── 📄 gradle.properties                # Gradle 속성
├── 📂 gradle/
│   ├── libs.versions.toml             # 버전 카탈로그
│   └── wrapper/
│
├── 📄 README.md                        # 메인 문서
├── 📄 IMPLEMENTATION_GUIDE.md          # 구현 가이드
├── 📄 SAMPLE_APP_GUIDE.md              # 샘플 앱 가이드
└── 📄 PROJECT_STRUCTURE.md             # 이 파일

```

## 📦 모듈 설명

### 1. server (Spring Boot)

**역할:** 백엔드 API 서버

**주요 클래스:**
- `DeepLinkController`: REST API 엔드포인트
- `DeepLinkService`: 비즈니스 로직
- `FingerprintUtil`: 디바이스 핑거프린팅

**API 엔드포인트:**
- `POST /api/v1/links` - 딥링크 생성
- `GET /d/{linkId}` - 링크 클릭 리다이렉션
- `POST /api/v1/match` - 디바이스 매칭
- `GET /api/v1/links/{linkId}/stats` - 통계 조회

### 2. library (Kotlin Multiplatform)

**역할:** Android/iOS 공용 SDK

**플랫폼:**
- `commonMain`: 공통 코드 (API 클라이언트, 모델)
- `androidMain`: Android 구현 (디바이스 정보 수집)
- `iosMain`: iOS 구현 (디바이스 정보 수집)

**주요 클래스:**
- `DeepLinkSDK`: SDK 메인 클래스
- `DeviceInfoProvider`: 플랫폼별 디바이스 정보 수집
- `DeepLinkSDKHelper`: 플랫폼별 헬퍼 (초기화, 매칭)

### 3. sample-android

**역할:** Android 샘플 애플리케이션

**주요 화면:**
- `MainActivity`: 딥링크 확인 및 테스트
- `ProductActivity`: 딥링크로 이동하는 화면

**특징:**
- Material Design 3
- ViewBinding 사용
- Coroutines 기반 비동기 처리
- 실시간 로그 표시

### 4. sample-ios

**역할:** iOS 샘플 애플리케이션

**주요 화면:**
- `ContentView`: 딥링크 확인 및 테스트
- `ProductView`: 딥링크로 이동하는 화면

**특징:**
- SwiftUI 기반
- MVVM 아키텍처
- Combine 사용
- 실시간 로그 표시

## 🔧 빌드 시스템

### Gradle 모듈

```kotlin
// settings.gradle.kts
include(":library")    // KMP SDK
include(":server")     // Spring Boot
include(":sample-android") // Android 샘플
```

### 의존성 관계

```
sample-android → library (KMP SDK)
sample-ios → library.framework (빌드 산출물)
server (독립적)
```

## 📝 문서 구조

### 메인 문서
- `README.md` - 프로젝트 전체 개요 및 가이드
- `PROJECT_STRUCTURE.md` - 프로젝트 구조 (이 파일)

### 가이드 문서
- `IMPLEMENTATION_GUIDE.md` - 기존 프로젝트 통합 가이드
- `SAMPLE_APP_GUIDE.md` - 샘플 앱 실행 및 테스트

### 모듈별 문서
- `server/README.md` - 서버 실행 및 설정
- `sample-android/README.md` - Android 샘플 앱
- `sample-ios/README.md` - iOS 샘플 앱 (상세)
- `sample-ios/SETUP_GUIDE.md` - iOS 빠른 시작

## 🗂️ 설정 파일

### Gradle
- `build.gradle.kts` - 루트 빌드 설정
- `settings.gradle.kts` - 모듈 포함 설정
- `gradle.properties` - Gradle 속성
- `gradle/libs.versions.toml` - 버전 카탈로그

### Server
- `server/src/main/resources/application.yml` - Spring 설정

### Android
- `sample-android/src/main/AndroidManifest.xml` - 앱 매니페스트
- `sample-android/proguard-rules.pro` - ProGuard 규칙

### iOS
- `sample-ios/Info.plist.example` - Info.plist 예제

## 🚀 빌드 명령어

### 서버
```bash
./gradlew :server:bootRun              # 개발 서버 실행
./gradlew :server:bootJar              # JAR 빌드
```

### Android
```bash
./gradlew :sample-android:installDebug # 디버그 설치
./gradlew :sample-android:assembleRelease # 릴리스 빌드
```

### iOS 프레임워크
```bash
# Simulator
./gradlew :library:linkDebugFrameworkIosSimulatorArm64

# Device
./gradlew :library:linkReleaseFrameworkIosArm64
```

### 라이브러리
```bash
./gradlew :library:build               # 전체 빌드
./gradlew :library:publish             # Maven 퍼블리시
```

## 📊 라인 수 통계

| 모듈 | 파일 수 | 대략적인 코드 라인 |
|------|---------|-------------------|
| server | 15+ | ~1,500 lines |
| library (common) | 3 | ~200 lines |
| library (android) | 3 | ~200 lines |
| library (ios) | 3 | ~150 lines |
| sample-android | 6+ | ~400 lines |
| sample-ios | 4 | ~600 lines |
| **전체** | **30+** | **~3,000+ lines** |

## 🎯 핵심 플로우

### 1. 딥링크 생성
```
Client → POST /api/v1/links → Server → Database
```

### 2. 링크 클릭
```
Browser → GET /d/{linkId} → Server (저장) → Redirect → App Store
```

### 3. 앱 첫 실행
```
App → SDK.checkDeferredDeepLink() → POST /api/v1/match → Server (매칭) → Response
```

## 🔐 환경별 설정

### 개발 (Development)
- Server: `http://localhost:8080`
- Database: Oracle Local
- HTTP 허용

### 스테이징 (Staging)
- Server: `https://staging.your-domain.com`
- Database: Oracle Staging
- HTTPS 강제

### 프로덕션 (Production)
- Server: `https://your-domain.com`
- Database: Oracle Production
- HTTPS 강제
- Rate Limiting 활성화

## 📚 추가 리소스

### 기술 문서
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [SwiftUI](https://developer.apple.com/xcode/swiftui/)

### 참고 프로젝트
- [Branch.io](https://branch.io/)
- [AppsFlyer](https://www.appsflyer.com/)

---

**Last Updated:** 2025-10-21

