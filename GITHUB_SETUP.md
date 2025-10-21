# GitHub 업로드 가이드

이 문서는 `deferred-deeplink-system` 프로젝트를 GitHub에 업로드하는 방법을 안내합니다.

## 🚀 1단계: GitHub에서 새 Repository 생성

1. **GitHub 웹사이트** 접속: https://github.com
2. 우측 상단 **+** 버튼 → **New repository** 클릭
3. Repository 설정:
   - **Repository name**: `deferred-deeplink-system`
   - **Description**: `Deferred Deep Link system with Kotlin Multiplatform SDK (Android/iOS) and Spring Boot server`
   - **Public** 또는 **Private** 선택
   - **⚠️ 주의**: "Initialize this repository with" 옵션들은 **모두 체크 해제** (이미 로컬에 코드가 있으므로)
4. **Create repository** 클릭

## 📝 2단계: 로컬 Git 초기화 및 커밋

터미널을 열고 프로젝트 디렉토리로 이동:

```bash
cd /Users/yulim/dev/workspace_android/multiplatform-library-template
```

### Git 초기화

```bash
# Git 초기화
git init

# 사용자 정보 설정 (아직 안했다면)
git config user.name "coooldoggy"
git config user.email "your-email@example.com"

# 또는 전역 설정
# git config --global user.name "coooldoggy"
# git config --global user.email "your-email@example.com"
```

### 파일 추가 및 커밋

```bash
# 모든 파일 스테이징
git add .

# 첫 커밋
git commit -m "Initial commit: Deferred Deep Link System

- Spring Boot server with device fingerprinting
- Kotlin Multiplatform SDK (Android/iOS)
- Android sample app
- iOS sample app (SwiftUI)
- Probabilistic matching algorithm (70%+ accuracy)
- Oracle database integration
- Complete documentation"
```

## 🌐 3단계: GitHub에 Push

GitHub에서 제공한 명령어를 사용합니다 (본인의 GitHub username으로 변경):

```bash
# Remote 추가
git remote add origin https://github.com/coooldoggy/deferred-deeplink-system.git

# Main 브랜치로 변경 (GitHub 기본 브랜치)
git branch -M main

# Push
git push -u origin main
```

## ✅ 4단계: 확인

브라우저에서 다음 URL로 접속하여 확인:
```
https://github.com/coooldoggy/deferred-deeplink-system
```

## 📌 추가 작업 (선택사항)

### GitHub Topics 추가

Repository 페이지에서 **About** 섹션의 ⚙️ 버튼 클릭 후 Topics 추가:
- `deferred-deep-link`
- `kotlin-multiplatform`
- `spring-boot`
- `android`
- `ios`
- `deep-linking`
- `attribution`
- `device-fingerprinting`

### GitHub Actions 설정 (CI/CD)

`.github/workflows/build.yml` 파일을 생성하여 자동 빌드 설정 가능:

```yaml
name: Build

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build with Gradle
      run: ./gradlew build
```

### README.md 업데이트

README.md에 GitHub 뱃지 추가:

```markdown
# Deferred Deep Link System

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-blue.svg)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green.svg)](https://spring.io/projects/spring-boot)

...
```

## 🔄 이후 업데이트 방법

코드를 수정한 후:

```bash
# 변경사항 확인
git status

# 스테이징
git add .

# 커밋
git commit -m "feat: 새로운 기능 추가"

# Push
git push origin main
```

### 커밋 메시지 컨벤션 (권장)

- `feat:` - 새로운 기능
- `fix:` - 버그 수정
- `docs:` - 문서 수정
- `style:` - 코드 포맷팅
- `refactor:` - 리팩토링
- `test:` - 테스트 추가
- `chore:` - 빌드 설정 등

## 🐛 문제 해결

### Permission denied (publickey) 에러

HTTPS 대신 사용하거나, SSH 키 설정:

```bash
# HTTPS 사용 (권장)
git remote set-url origin https://github.com/coooldoggy/deferred-deeplink-system.git
```

### Push 거부됨 (rejected)

```bash
# Force push (주의: 협업 시 사용 금지)
git push -f origin main

# 또는 Pull 후 Push
git pull origin main --rebase
git push origin main
```

### Large file 경고

```bash
# Git LFS 사용
git lfs install
git lfs track "*.jar"
git add .gitattributes
git commit -m "chore: Add Git LFS"
```

## 📞 지원

문제가 발생하면:
1. GitHub Issues: https://github.com/coooldoggy/deferred-deeplink-system/issues
2. README.md 참고
3. 각 모듈별 README 참고

---

**축하합니다! 🎉**

프로젝트가 성공적으로 GitHub에 업로드되었습니다.

