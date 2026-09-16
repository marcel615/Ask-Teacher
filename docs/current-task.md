# Current Task

## Issue

- Issue: #33
- Title: test: 전체 API 자동 테스트 구축 및 수동 테스트 지침 전환
- URL: https://github.com/marcel615/Ask-Teacher/issues/33
- State: OPEN
- Labels: 없음

## 목표

현재 구현된 모든 API에 대해 Controller, Service, Repository 테스트와
통합 테스트를 구축한다.

기존 .http 파일의 시나리오는 참고 자료로 활용하고,
자동 테스트 전환 후 해당 파일을 삭제한다.
앞으로 API를 추가하거나 변경할 때 테스트 클래스 기반 자동 테스트를
작성·실행하고 결과를 기록하도록 작업 지침을 정비한다.

## 범위

- 현재 구현된 인증, 카테고리, 게시글, 게시글 좋아요 API 전체
- 게시글 첨부파일, 검색, 페이징, 카테고리 필터 포함
- 기존 자동 테스트 검토 및 보완
- Controller, Service, Repository별 책임을 검증하는 테스트 작성
- HTTP 요청부터 인증, 비즈니스 로직, DB 처리까지 검증하는 통합 테스트 작성
- 테스트용 DB, 인증 설정 및 파일 저장 경로 격리
- 기존 .http 파일 9개 삭제
- 관련 작업 지침과 Issue/PR 템플릿의 자동 테스트 기준 정비
- API별 테스트 대상·시나리오·대응 테스트 클래스 기록
- 인증 실패 응답 불일치 수정:
  - 보호된 경로에 토큰 없이 접근하거나 유효하지 않은 토큰으로 접근하면
    Security의 AuthenticationEntryPoint에서 401 Unauthorized를 반환한다.
  - 기존 ErrorResponse 형식의 JSON 본문을 반환한다.
  - status는 401, message는 "인증이 필요합니다."로 한다.
  - 공통 Security 필터 체인의 미인증 접근 거부에 적용한다.
  - 인증된 사용자의 작성자 불일치 403과 공개 API 접근 정책은 유지한다.

## 제외 범위

- 신규 API 및 기능 추가
- 기존 API 동작, 요청·응답 규격, Validation 및 에러 정책 변경
  - 예외: 이번에 승인한 미인증 접근의 401 및 오류 JSON 응답 처리
- 운영 Entity, DB 구조, 인증·인가 구조 변경
  - 예외: 위 응답 처리를 위한 최소한의 Security 예외 처리 설정
  - JWT 발급·검증, 인증 정보 구성, 접근 허용 규칙 변경은 제외
- 테스트 자동화와 무관한 운영 코드 리팩터링
- CI 파이프라인 구성
- 성능·부하 테스트
- 과거 devlog에 기록된 수동 확인 이력의 소급 수정
- 기존 기능 버그 수정
  - 위 인증 실패 응답 불일치만 이번 Issue에서 수정한다.
  - 그 밖의 버그는 기록하고 별도 Issue로 분리한다.

기존 버그나 명세와 구현의 동작 불일치를 발견하면 임의로 기대값을 바꾸거나
테스트를 비활성화하여 통과시키지 않는다.
영향받는 시나리오와 완료 조건을 사용자에게 보고하고 처리 방향을 협의한다.
별도 Issue 생성은 승인 후 진행한다.

## 요구사항 변경 요약

- docs/requirements.md 변경 없음
- 사유: 사용자 기능과 비즈니스 규칙은 유지한다.
- 자동 테스트 작성·실행 의무는 AGENTS.md와 관련 작업 지침에 반영한다.
- 참조: docs/requirements.md

## API 변경 요약

- docs/api-spec.md 변경 없음
- 신규 API 및 기존 API 계약 변경 없음
- 성공 응답 DTO, 페이지 응답, 본문 없는 응답과 기존 오류 응답을 검증한다.
- 참조: docs/api-spec.md
- 보호된 경로의 미인증 응답을 실제 403에서 기존 명세의 401로 수정한다.
- 기존 공통 오류 응답 형식인 status, message를 사용한다.
- API 명세의 상태 코드 및 응답 구조 변경은 없다.

### 대상 API 및 주요 검증 시나리오

| API | 성공 응답 | 주요 검증 |
|---|---|---|
| POST /api/auth/signup | 201, 가입 DTO | 가입, 비밀번호 암호화, 이메일·닉네임 중복, 입력 검증 |
| POST /api/auth/login | 200, 토큰 DTO | 로그인, 발급 토큰 사용, 잘못된 이메일·비밀번호, 입력 검증 |
| GET /api/categories | 200, 목록 | 비로그인 조회, 목록 매핑, 빈 목록 |
| POST /api/posts | 201, 작성 DTO | 인증 사용자 작성, multipart, 첨부 유무, 초기 상태, 입력·카테고리·파일 검증 |
| GET /api/posts | 200, 페이지 DTO | 검색, 카테고리 필터, 조건 조합, 공백 검색어, 페이징, 최신순, 삭제 제외, 잘못된 조회 조건 |
| GET /api/posts/{postId} | 200, 상세 DTO | 비로그인·로그인 조회, likedByMe, 좋아요 수, 첨부파일 정보, 없거나 삭제된 게시글 |
| PATCH /api/posts/{postId} | 200, 수정 DTO | 작성자 수정, multipart, 첨부 처리, 변경·유지 필드, updatedAt, 입력 오류, 작성자 불일치, 데이터 없음 |
| DELETE /api/posts/{postId} | 204, 본문 없음 | 작성자 삭제, soft delete, updatedAt, 조회 제외, 작성자 불일치, 없거나 이미 삭제된 게시글 |
| POST /api/posts/{postId}/likes | 200, 본문 없음 | 등록, 좋아요 수 증가, 중복 등록, 없거나 삭제된 게시글 |
| DELETE /api/posts/{postId}/likes | 204, 본문 없음 | 취소, 좋아요 수 감소, 취소 대상 없음, 없거나 삭제된 게시글 |

- 인증이 필요한 API는 토큰 누락·유효하지 않은 토큰 등 인증 실패를 검증한다.
- 공개 조회 API는 인증 없이 접근할 수 있음을 검증한다.
- 사용자 없음 등 Service에서 처리하는 실패 조건도 해당 계층에서 검증한다.
- 위 목록을 기준으로 구체적인 테스트 메서드와 대응 관계를
  docs/current-work-log.md에 기록한다.

## ERD 변경

- ERD 변경 없음
- 사유: 테스트 자동화, 테스트 실행 환경 및 미인증 응답 처리를 변경하며,
  Entity, 운영 DB 테이블, 관계, 제약조건은 변경하지 않는다.
- 테스트는 기존 User, Category, Post, PostLike, PostFile 매핑을 사용한다.
- 참조: docs/erd.md

## 테스트 설계

### Controller 테스트

- 서비스 의존성을 대체하여 웹 계층을 분리해 검증한다.
- 요청 매핑, JSON 및 multipart 바인딩, Validation을 검증한다.
- HTTP 상태 코드, 응답 DTO·목록·페이지 구조, 빈 응답 본문을 검증한다.
- 예외 처리와 인증 사용자 ID 전달을 검증한다.
- Controller 메서드 직접 호출만으로 웹 계층 검증을 대체하지 않는다.

### Service 단위 테스트

- Repository, 비밀번호 처리, 토큰 발급, 파일 저장 등 의존성을
  필요한 범위에서 mock으로 대체한다.
- 비즈니스 규칙, 데이터 변경, 정상 처리와 실패 조건을 검증한다.
- AuthService, CategoryService, PostService, PostLikeService를 대상으로 한다.
- 기존 PostServiceTest는 Spring 전체 컨텍스트와 DB를 사용하는 테스트이므로,
  기존 검증을 보존하면서 단위 테스트와 통합 테스트 역할을 구분한다.

### Repository 테스트

- UserRepository, CategoryRepository, PostRepository,
  PostLikeRepository, PostFileRepository를 대상으로 한다.
- 테스트용 DB를 사용하는 JPA 슬라이스 테스트로 구성한다.
- Repository 자체를 mock 처리하는 방식으로 쿼리 검증을 대체하지 않는다.
- 실제 사용하는 저장·조회·변경 동작과 Entity 매핑을 검증한다.
- 검색 조건, 페이징·정렬, 삭제 제외, 좋아요 수 증감,
  사용자·게시글별 좋아요 조회, 첨부파일 조회 순서를 검증한다.
- 기존 유니크 제약 등 DB 제약은 flush 및 재조회로 검증한다.
- Repository 테스트는 DB를 사용하는 계층 테스트임을 명시한다.

### API 통합 테스트

- 실제 애플리케이션 구성, Security, Service, Repository를 연결해 검증한다.
- 모든 대상 API의 정상 흐름과 주요 실패 흐름을 포함한다.
- 회원가입 → 로그인 → 발급 토큰을 사용한 요청을 검증한다.
- 게시글 작성 → 조회 → 수정 → 삭제 흐름을 검증한다.
- 좋아요 등록·취소 결과가 목록·상세 응답 및 DB에 반영되는지 검증한다.
- 첨부파일 저장 결과와 상세 조회의 파일 정보를 검증한다.
- MockMvc 기반 통합 테스트를 기본으로 하되,
  서블릿의 multipart 업로드 제한처럼 실제 HTTP 처리가 필요한 검증은
  RANDOM_PORT 서버를 테스트가 직접 시작하여 확인한다.
- 수동 서버 기동이나 .http 실행을 완료 조건으로 사용하지 않는다.

### 첨부파일 검증

- PostFileStorage 테스트를 포함한다.
- 허용 MIME 타입, 빈 파일, 크기 경계값·초과, 저장 실패를 검증한다.
- 정상 저장 시 파일과 메타데이터를 확인한다.
- 실제 저장은 테스트 전용 임시 디렉터리를 사용한다.
- 애플리케이션의 파일 크기 검증과 서블릿 업로드 제한을 구분하여 검증한다.

### 테스트 환경과 격리

- 테스트 프로필과 H2 메모리 DB를 사용한다.
- 개발·운영 DB와 기존 uploads 경로를 사용하지 않는다.
- 테스트 전용 JWT 설정을 사용한다.
- DB 데이터와 임시 파일은 테스트별로 준비하고 정리한다.
- 실제 HTTP 서버를 사용하는 테스트는 테스트 메서드의 트랜잭션 롤백만으로
  서버가 저장한 데이터가 정리된다고 가정하지 않는다.
- 고정 ID, 기존 데이터, 실행 순서 및 외부 서버에 의존하지 않는다.
- H2 검증이 MySQL 고유 동작 검증을 대신하지 않는다는 한계를 기록한다.
- build.gradle의 기존 테스트 의존성을 우선 활용한다.

## Validation

- 현재 Request DTO의 필수값, 형식, 길이 및 경계값을 검증한다.
- page, size 등 조회 조건의 기존 검증을 포함한다.
- 신규 Validation 규칙을 추가하거나 기존 규칙을 변경하지 않는다.

## 예외 처리

- 기존 비즈니스 예외, HTTP 상태 코드와 에러 응답을 검증한다.
- 입력 오류, 인증 실패, 작성자 불일치, 대상 없음, 중복 요청,
  파일 검증 실패 및 파일 저장 실패를 포함한다.
- Security의 AuthenticationEntryPoint에 미인증 응답 처리를 추가한다.
- ErrorCode.UNAUTHORIZED:
  - HTTP 상태: 401 Unauthorized
  - 메시지: "인증이 필요합니다."
- 응답은 ErrorResponse를 JSON으로 직렬화하고,
  Content-Type 및 UTF-8 인코딩을 명시한다.
- GlobalExceptionHandler, 기존 로그인 실패와 작성자 불일치 응답은 유지한다.
- JWT 필터·토큰 검증 로직 및 별도 AccessDeniedHandler 변경은 포함하지 않는다.

## 작업 지침 변경

- AGENTS.md:
  - 수동 API 확인 절을 자동 테스트 기준으로 교체한다.
  - 새 API·변경 API의 관련 계층 테스트와 통합 테스트 작성 의무를 명시한다.
  - ./gradlew test 실행 및 결과 기록 기준을 명시한다.
  - 빠른 지시문 참조 경로를 docs/prompts/quick.md로 바로잡는다.
- docs/prompts/quick.md:
  - .http 작성·수동 확인 지시를 자동 테스트 작성·실행 지시로 변경한다.
  - 보고 항목을 자동 테스트 범위·결과·미검증 사항 기준으로 변경한다.
- docs/review-checklist.md:
  - 수동 API 확인 절을 계층별 테스트, 통합 테스트, 환경 격리,
    실행 결과 확인 항목으로 교체한다.
- .agents/skills/issue-driven-dev/SKILL.md:
  - Builder의 테스트 작성·실행·기록 및 Architect의 검증 기준을 명시한다.
- .github/ISSUE_TEMPLATE/feature_request.yml:
  - 완료 조건 예시의 수동 API 확인을 자동 테스트 작성·통과 기준으로 변경한다.
- .github/PULL_REQUEST_TEMPLATE/pull_request_template.md:
  - 직접 실행 확인 항목을 자동 테스트 범위·실행 결과 확인 항목으로 변경한다.
- 다른 문서에서 현재 적용되는 수동 확인 의무를 발견하면
  해당 문서와 수정 내용을 기록하고 같은 기준으로 정비한다.
- 과거 수행 기록과 API 명세의 HTTP 요청 예시는 삭제 대상이 아니다.

## 예상 변경 파일

### Architect 사전 반영 문서

- docs/current-task.md

이번 초안 승인 후 Architect가 반영하는 파일이다.
requirements.md, api-spec.md, erd.md는 변경이 필요하지 않아 포함하지 않는다.

### Builder 구현 변경 예상 파일

아래 Java 경로의 기준은
src/test/java/com/github/marcel615/askteacher/ 이다.

- domain/auth/controller/AuthControllerTest.java
- domain/auth/service/AuthServiceTest.java
- domain/category/controller/CategoryControllerTest.java
- domain/category/service/CategoryServiceTest.java
- domain/category/repository/CategoryRepositoryTest.java
- domain/user/repository/UserRepositoryTest.java
- domain/post/controller/PostControllerTest.java
- domain/post/service/PostServiceTest.java
- domain/post/repository/PostRepositoryTest.java
- domain/post/repository/PostFileRepositoryTest.java
- domain/post/storage/PostFileStorageTest.java
- domain/postlike/controller/PostLikeControllerTest.java
- domain/postlike/service/PostLikeServiceTest.java
- domain/postlike/repository/PostLikeRepositoryTest.java
- integration/*IntegrationTest.java
  - 인증、카테고리、게시글、좋아요 및 실제 HTTP 업로드 제한 검증
- integration/ApiIntegrationTest.java
  - 보호 API 5개 × 토큰 누락·무효 10개 시나리오에서
    401, JSON Content-Type, status, message를 검증한다.
  - 작성자 불일치 403 및 기존 오류 본문을 검증한다.
  - 공개 API 접근과 로그인 실패 응답의 회귀 여부를 검증한다.
- support/*.java
  - 필요한 최소한의 테스트 데이터 및 환경 지원 코드
- AskteacherApplicationTests.java
- global/config/SecurityConfigTest.java
  - 기존 isIn(401, 403) 검증을 정확한 401 검증으로 강화한다.
  - 실제 HTTP 응답의 JSON 본문과 Content-Type을 검증한다.
- global/security/jwt/JwtTokenProviderTest.java
  - 기존 테스트는 필요한 보완 범위에서 수정하며 기존 검증을 유지한다.

미인증 응답 수정에 한해 허용하는 운영 코드:

- src/main/java/com/github/marcel615/askteacher/global/config/SecurityConfig.java
  - AuthenticationEntryPoint 설정 및 401 JSON 응답 처리
- src/main/java/com/github/marcel615/askteacher/global/exception/ErrorCode.java
  - UNAUTHORIZED 추가

테스트 설정 및 작업 지침:

- src/test/resources/application-test.yaml
- build.gradle
  - 테스트 실행에 필요한 설정·의존성 보완이 확인된 경우에 한정한다.
- AGENTS.md
- docs/prompts/quick.md
- docs/review-checklist.md
- .agents/skills/issue-driven-dev/SKILL.md
- .github/ISSUE_TEMPLATE/feature_request.yml
- .github/PULL_REQUEST_TEMPLATE/pull_request_template.md
- docs/current-work-log.md
  - 구현 시작 시 Issue #33 기준으로 초기화하고 테스트 대응표와 결과를 기록한다.
- 추가로 발견한 현재 적용 중인 수동 API 확인 지침 문서
  - 발견 경로와 변경 사유를 작업 기록에 남긴다.

삭제 대상:
src/main/java/com/github/marcel615/askteacher/http/ 아래

- auth/signup.http
- auth/login.http
- category/category.http
- post/createPost.http
- post/listPosts.http
- post/detailPost.http
- post/updatePost.http
- post/deletePost.http
- post/likePost.http

운영 코드 변경은 위 SecurityConfig.java와 ErrorCode.java의
미인증 응답 처리에 한정한다.
그 밖의 운영 코드 및 운영 설정 파일 변경이 필요하면
사유와 대안을 제시하고 범위를 먼저 협의한다.

## 구현 및 검증 순서

1. 기존 테스트와 .http 시나리오를 확인하고 API별 테스트 대응표를 작성한다.
2. 테스트용 DB, 인증 설정, 임시 파일 저장 환경을 구성한다.
3. 계층별 테스트와 통합 테스트를 작성·보완한다.
4. API별 정상·실패 시나리오의 누락 여부를 확인한다.
5. 자동 테스트 전환을 확인한 뒤 기존 .http 파일을 삭제한다.
6. 관련 지침과 템플릿을 자동 테스트 기준으로 변경한다.
7. ./gradlew test로 전체 테스트를 실행한다.
8. 전체 테스트를 다시 실행해 데이터·파일 잔존으로 인한 실패가 없는지 확인한다.
9. 테스트 대상, 실행 결과 및 미검증 사항을 작업 기록과 PR 요약에 남긴다.

Windows PowerShell에서는 같은 Gradle test 작업을
.\gradlew.bat test로 실행할 수 있다.

## 완료 조건

- [ ] 보호 API 5개에서 토큰 누락·무효 10개 시나리오가 모두
      401과 합의된 JSON 오류 본문을 반환한다.
- [ ] 실제 HTTP 요청에서도 401, JSON Content-Type 및 오류 본문을 확인했다.
- [ ] 인증된 비작성자의 수정·삭제는 기존 403과 오류 본문을 유지한다.
- [ ] 공개 API 접근 및 로그인 정보 불일치 응답이 유지된다.
- [ ] SecurityConfigTest에서 401 또는 403을 모두 허용하는 검증을 제거했다.
- [ ] 기존 104개 테스트와 추가한 회귀 테스트가 모두 통과한다.
- [ ] 테스트를 실제 재실행하여 전체 통과를 확인하고,
      실행 수·실패 수·skip 수를 기록했다.
- [ ] 전체 10개 API와 관련 계층의 테스트 대응표가 작성되었다.
- [ ] Controller 4개, Service 4개, Repository 5개를 대상으로 테스트가 작성·보완되었다.
- [ ] 전체 API의 정상 흐름과 주요 실패 흐름에 대한 통합 테스트가 작성되었다.
- [ ] 검색·페이징·삭제 제외·좋아요·첨부파일 동작이 검증되었다.
- [ ] 기존 자동 테스트의 검증 범위가 유지되었다.
- [ ] 테스트 DB·인증 설정·파일 저장 경로가 개발·운영 환경과 격리되었다.
- [ ] ./gradlew test 전체 통과 및 반복 실행을 확인했다.
- [ ] 기존 .http 파일 9개를 삭제했다.
- [ ] 관련 작업 지침과 Issue/PR 템플릿을 자동 테스트 기준으로 변경했다.
- [ ] 현재 적용되는 수동 API 확인 의무가 남아 있는지 검색·검토했다.
- [ ] 테스트 범위·실행 결과·미검증 사항을 PR 요약 또는 devlog에 기록했다.
- [ ] 발견된 기존 버그와 명세 불일치의 처리 상태를 기록했으며,
      미해결 사항이 있다면 완료 여부를 사용자와 협의했다.
