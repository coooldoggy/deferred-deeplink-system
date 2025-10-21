# Deferred Deep Link Server

Spring Boot 기반 Deferred Deep Link 서버 구현

## 빠른 시작

### 1. Oracle 데이터베이스 설정

```sql
CREATE USER deeplink IDENTIFIED BY deeplink;
GRANT CONNECT, RESOURCE TO deeplink;
GRANT UNLIMITED TABLESPACE TO deeplink;
```

### 2. application.yml 수정

`src/main/resources/application.yml` 파일의 데이터베이스 설정을 수정하세요.

### 3. 실행

```bash
# 개발 모드
../gradlew bootRun

# 프로덕션 JAR 빌드
../gradlew bootJar

# 실행
java -jar build/libs/server-1.0.0.jar
```

## API 엔드포인트

- `POST /api/v1/links` - 딥링크 생성
- `GET /d/{linkId}` - 딥링크 클릭 (리다이렉션)
- `POST /api/v1/match` - 디바이스 매칭
- `GET /api/v1/links/{linkId}/stats` - 통계 조회

자세한 내용은 상위 README.md를 참고하세요.

