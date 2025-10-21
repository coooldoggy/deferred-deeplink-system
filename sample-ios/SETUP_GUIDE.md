# iOS 샘플 앱 빠른 설정 가이드

이 가이드는 iOS 샘플 앱을 최대한 빠르게 실행하기 위한 단계별 지침입니다.

## ⚡ 빠른 시작 (5분)

### 1️⃣ 서버 실행 (1분)

```bash
cd /Users/yulim/dev/workspace_android/multiplatform-library-template
./gradlew :server:bootRun
```

서버가 `http://localhost:8080`에서 실행될 때까지 기다립니다.

### 2️⃣ KMP 프레임워크 빌드 (1분)

새 터미널 창에서:

```bash
cd /Users/yulim/dev/workspace_android/multiplatform-library-template
./gradlew :library:linkDebugFrameworkIosSimulatorArm64
```

빌드가 완료되면 프레임워크가 생성됩니다:
```
library/build/bin/iosSimulatorArm64/debugFramework/library.framework
```

### 3️⃣ Xcode 프로젝트 생성 (2분)

1. **Xcode 실행**

2. **Create a new Xcode project**

3. **iOS → App** 선택, **Next**

4. 프로젝트 설정:
   ```
   Product Name: DeepLinkSample
   Team: (본인 계정)
   Organization Identifier: com.coooldoggy.deeplink
   Interface: SwiftUI
   Language: Swift
   ```
   **Next**

5. 저장 위치:
   ```
   /Users/yulim/dev/workspace_android/multiplatform-library-template/sample-ios/
   ```
   **Create**

### 4️⃣ 프레임워크 추가 (1분)

1. **Project Navigator**에서 프로젝트 선택

2. **Targets → DeepLinkSample** 선택

3. **General** 탭 → **Frameworks, Libraries, and Embedded Content**

4. **+ 버튼** 클릭

5. **Add Other... → Add Files...**

6. 다음 경로로 이동:
   ```
   library/build/bin/iosSimulatorArm64/debugFramework/
   ```

7. **library.framework** 선택, **Open**

8. **Embed & Sign** 선택

### 5️⃣ Swift 파일 추가 (1분)

1. **기존 파일 삭제**:
   - Project Navigator에서 `ContentView.swift` 삭제
   - `DeepLinkSampleApp.swift` 삭제

2. **새 파일 추가**:
   - Project Navigator에서 프로젝트 우클릭
   - **Add Files to "DeepLinkSample"...**
   - 다음 경로로 이동:
     ```
     sample-ios/SampleApp/
     ```
   - 모든 `.swift` 파일 선택 (Cmd 누르고 클릭):
     - SampleApp.swift
     - ContentView.swift
     - DeepLinkViewModel.swift
     - ProductView.swift
   - **Copy items if needed** 체크
   - **Add** 클릭

### 6️⃣ Info.plist 설정 (30초)

1. **Info** 탭 이동

2. **우클릭 → Add Row**

3. 다음 추가:
   ```
   Key: App Transport Security Settings
   Type: Dictionary
   ```

4. App Transport Security Settings **확장** → **우클릭 → Add Row**
   ```
   Key: Allow Arbitrary Loads
   Type: Boolean
   Value: YES
   ```

5. **Info** 탭에서 **URL Types** 섹션

6. **+ 버튼** 클릭
   ```
   Identifier: com.coooldoggy.deeplink.sample
   URL Schemes: coooldoggy
   Role: Editor
   ```

### 7️⃣ 빌드 및 실행 (30초)

1. **Simulator 선택**: iPhone 14 Pro

2. **Product → Run** 또는 **Cmd + R**

3. 앱이 실행되고 자동으로 딥링크 확인 시작!

## ✅ 테스트

### 링크 생성하고 테스트하기

앱이 실행되면:

1. **"테스트 링크 열기"** 버튼 클릭
2. Safari가 열리면 자동으로 앱스토어로 리다이렉트
3. 앱으로 돌아가서 **"초기화 후 확인"** 클릭
4. 매칭 결과 확인!

### 실제 링크로 테스트

터미널에서:

```bash
# 링크 생성
curl -X POST http://localhost:8080/api/v1/links \
  -H "Content-Type: application/json" \
  -d '{
    "targetUrl": "myapp://product/summer",
    "campaignName": "Summer Sale",
    "customData": {"discount": "50"}
  }'

# 응답에서 linkId 복사 후
# Safari에서 접속: http://localhost:8080/d/{linkId}
```

그 다음:
1. 앱에서 **"초기화 후 확인"** 클릭
2. 매칭 성공 확인!
3. Match Score 70% 이상이면 성공

## 🐛 문제 해결

### "Module 'SharedLibrary' not found"

```bash
# 1. Clean Build
Product → Clean Build Folder (Cmd + Shift + K)

# 2. 프레임워크 재빌드
./gradlew :library:linkDebugFrameworkIosSimulatorArm64

# 3. Xcode에서 프레임워크 재추가
```

### "Network error"

1. 서버가 실행 중인지 확인:
   ```bash
   curl http://localhost:8080/api/v1/links
   # 404 응답이 나오면 서버 정상
   ```

2. Info.plist에 ATS 설정 확인

### 빌드 에러

```bash
# Derived Data 삭제
rm -rf ~/Library/Developer/Xcode/DerivedData

# Xcode 재시작
```

## 🎉 완료!

앱이 실행되면:
- ✅ 자동으로 딥링크 확인
- ✅ 로그에서 진행 상황 확인
- ✅ 매칭 결과 실시간 표시
- ✅ 테스트 도구 사용 가능

더 자세한 내용은 [README.md](./README.md)를 참고하세요.

