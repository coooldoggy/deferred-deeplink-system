# iOS 샘플 앱

Deferred Deep Link SDK를 사용하는 iOS 샘플 앱입니다.

## 📱 Xcode 프로젝트 생성

### 1단계: Xcode에서 새 프로젝트 생성

1. **Xcode 실행**
2. **Create a new Xcode project**
3. **iOS → App** 선택
4. 프로젝트 설정:
   - Product Name: `DeepLinkSample`
   - Team: 본인 계정 선택
   - Organization Identifier: `com.deeplink`
   - Interface: **SwiftUI**
   - Language: **Swift**
   - Storage: None
   - Include Tests: 선택 (옵션)

5. 저장 위치: 
   ```
   /Users/yulim/dev/workspace_android/multiplatform-library-template/sample-ios/
   ```

### 2단계: KMP 프레임워크 빌드

터미널에서:

```bash
cd /Users/yulim/dev/workspace_android/multiplatform-library-template

# Simulator용 프레임워크 빌드
./gradlew :library:linkDebugFrameworkIosSimulatorArm64

# 실제 디바이스용 프레임워크 빌드 (선택)
./gradlew :library:linkReleaseFrameworkIosArm64
```

**프레임워크 위치:**
- Simulator: `library/build/bin/iosSimulatorArm64/debugFramework/library.framework`
- Device: `library/build/bin/iosArm64/releaseFramework/library.framework`

### 3단계: 프레임워크를 Xcode에 추가

1. **Xcode에서 프로젝트 선택**
2. **Targets → DeepLinkSample** 선택
3. **General 탭**으로 이동
4. **Frameworks, Libraries, and Embedded Content** 섹션
5. **+ 버튼** 클릭
6. **Add Other... → Add Files...** 선택
7. 프레임워크 경로로 이동하여 `library.framework` 선택
8. **Embed & Sign** 선택

### 4단계: Framework Search Paths 설정

1. **Build Settings** 탭
2. **Search Paths** 섹션
3. **Framework Search Paths**에 추가:
   ```
   $(PROJECT_DIR)/../library/build/bin/iosSimulatorArm64/debugFramework
   ```

또는 상대 경로:
```
$(SRCROOT)/../library/build/bin/iosSimulatorArm64/debugFramework
```

### 5단계: Swift 파일 추가

이 디렉토리의 `SampleApp/` 폴더에 있는 Swift 파일들을 Xcode 프로젝트에 추가:

1. **SampleApp.swift** (앱 진입점)
2. **ContentView.swift** (메인 화면)
3. **DeepLinkViewModel.swift** (뷰모델)
4. **ProductView.swift** (상품 화면)

**Xcode에 추가하는 방법:**
1. Xcode Project Navigator에서 프로젝트 우클릭
2. **Add Files to "DeepLinkSample"...**
3. 파일들 선택하고 **Copy items if needed** 체크
4. **Add** 클릭

### 6단계: Info.plist 설정

#### App Transport Security (테스트용)

HTTP 통신 허용을 위해 `Info.plist`에 추가:

```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
</dict>
```

**또는** 특정 도메인만 허용:

```xml
<key>NSAppTransportSecurity</key>
<dict>
    <key>NSExceptionDomains</key>
    <dict>
        <key>localhost</key>
        <dict>
            <key>NSExceptionAllowsInsecureHTTPLoads</key>
            <true/>
        </dict>
    </dict>
</dict>
```

#### URL Scheme 설정

Deep Link 처리를 위해 URL Scheme 추가:

1. **Info 탭** 이동
2. **URL Types** 섹션에서 **+** 클릭
3. 설정:
   - **Identifier**: `com.deeplink.sample`
   - **URL Schemes**: `myapp`
   - **Role**: Editor

### 7단계: 빌드 및 실행

1. **Simulator 선택** (iPhone 14 Pro 권장)
2. **Product → Run** 또는 `Cmd + R`

## 🧪 테스트

### 시나리오 1: 매칭 성공 테스트

#### 1. 서버 실행
```bash
cd /Users/yulim/dev/workspace_android/multiplatform-library-template
./gradlew :server:bootRun
```

#### 2. 링크 생성
```bash
curl -X POST http://localhost:8080/api/v1/links \
  -H "Content-Type: application/json" \
  -d '{
    "targetUrl": "myapp://product/123",
    "campaignName": "iOS Test",
    "customData": {"discount": "50"}
  }'
```

#### 3. Safari에서 링크 클릭

Simulator의 Safari에서:
```
http://localhost:8080/d/{linkId}
```

#### 4. 앱에서 확인

앱에서 "초기화 후 확인" 버튼을 클릭하여 매칭 결과를 확인합니다.

### 시나리오 2: 실제 디바이스 테스트

#### 1. Mac의 로컬 IP 확인
```bash
ifconfig | grep "inet "
# 예: 192.168.1.100
```

#### 2. SampleApp.swift에서 서버 URL 변경
```swift
static let serverUrl = "http://192.168.1.100:8080"
```

#### 3. 실제 디바이스용 프레임워크 빌드
```bash
./gradlew :library:linkReleaseFrameworkIosArm64
```

#### 4. Xcode 설정 변경
- **Framework Search Paths**를 디바이스용 경로로 변경
- 프레임워크를 다시 추가

#### 5. 디바이스에서 실행

## 📱 UI 구성

### 메인 화면 (ContentView)

1. **헤더**
   - 앱 제목과 설명
   - 파란색 배경

2. **Progress Indicator**
   - 로딩 중 표시

3. **Result Card**
   - 매칭 성공/실패 결과
   - 타겟 URL, 캠페인 정보
   - Match Score
   - Custom Data

4. **Action Buttons**
   - 다시 확인
   - 초기화 후 확인
   - 테스트 링크 열기

5. **Log View**
   - 실시간 로그
   - 타임스탬프 포함
   - 아이콘으로 구분

### 상품 화면 (ProductView)

- 딥링크로 진입한 상품 정보 표시
- 할인 정보
- 캠페인 정보
- Custom Data

## 🔧 고급 설정

### 자동 프레임워크 임베딩 (Run Script)

매번 수동으로 프레임워크를 추가하지 않고, 빌드 시 자동으로 최신 프레임워크를 사용하도록 설정:

1. **Build Phases** 탭
2. **+ → New Run Script Phase**
3. 다음 스크립트 추가:

```bash
cd "$SRCROOT/.."
./gradlew :library:linkDebugFrameworkIosSimulatorArm64
```

4. **Output Files**에 추가:
```
$(SRCROOT)/../library/build/bin/iosSimulatorArm64/debugFramework/library.framework
```

### Universal Framework (선택)

Simulator와 실제 디바이스를 모두 지원하는 XCFramework 생성:

```bash
# library/build.gradle.kts에 추가
kotlin {
    val xcf = XCFramework("SharedLibrary")
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "SharedLibrary"
            xcf.add(this)
        }
    }
}
```

빌드:
```bash
./gradlew :library:assembleSharedLibraryXCFramework
```

## 🐛 트러블슈팅

### "Module 'SharedLibrary' not found"

**원인:** 프레임워크가 제대로 추가되지 않음

**해결:**
1. Framework Search Paths 확인
2. 프레임워크 재추가 (Embed & Sign)
3. Clean Build Folder (Cmd + Shift + K)
4. Derived Data 삭제

### "Unsupported Swift version"

**원인:** Swift 버전 불일치

**해결:**
1. Build Settings → Swift Language Version 확인
2. Xcode 업데이트

### 네트워크 에러

**원인:** App Transport Security 설정 누락 또는 서버 미실행

**해결:**
1. Info.plist에 ATS 설정 확인
2. 서버가 실행 중인지 확인
3. 방화벽 확인

### 시뮬레이터에서 localhost 접근 안됨

**해결:**
- 시뮬레이터는 호스트의 localhost를 그대로 사용 가능
- `http://localhost:8080` 사용

### 실제 디바이스에서 매칭 안됨

**해결:**
1. Mac과 디바이스가 같은 WiFi에 연결되어 있는지 확인
2. Mac의 로컬 IP를 정확히 설정했는지 확인
3. 방화벽에서 8080 포트 허용

## 📊 디버깅

### Xcode Console

로그 출력:
```swift
print("DeepLink: \(message)")
```

### LLDB 디버거

브레이크포인트 설정:
1. 코드 라인 번호 옆 클릭
2. Run하면 해당 지점에서 멈춤

### Network 모니터링

**Instruments** 사용:
1. Product → Profile (Cmd + I)
2. Network 템플릿 선택
3. 네트워크 트래픽 확인

## 🚀 배포 준비

### 1. 프로덕션 URL 설정

`SampleApp.swift`:
```swift
static let serverUrl = "https://your-production-domain.com"
```

### 2. ATS 설정 제거

`Info.plist`에서 `NSAllowsArbitraryLoads` 제거 (HTTPS만 사용)

### 3. Release 빌드

```bash
./gradlew :library:linkReleaseFrameworkIosArm64
```

### 4. Archive

1. **Product → Archive**
2. Organizer에서 **Distribute App**
3. App Store Connect 또는 Ad Hoc 선택

## 📚 참고 자료

- [Kotlin Multiplatform Mobile 문서](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html)
- [SwiftUI 튜토리얼](https://developer.apple.com/tutorials/swiftui)
- [KMP iOS 프레임워크 가이드](https://kotlinlang.org/docs/multiplatform-mobile-integrate-in-existing-app.html)

## 💡 팁

### 빠른 개발 사이클

1. **Hot Reload 활용**
   - SwiftUI Preview 사용
   - Cmd + Opt + P로 Preview 리프레시

2. **프레임워크 자동 빌드**
   - Run Script Phase 활용
   - 코드 변경 시 자동으로 최신 프레임워크 사용

3. **로그 활성화**
   - ViewModel에서 상세 로그 출력
   - Console에서 실시간 확인

---

**즐거운 코딩 되세요! 🎉**

