# Current Task

## 작업과 승인 근거

- Issue: [#79 로그인·로그아웃 보안 처리 구조 정리](https://github.com/marcel615/Ask-Teacher-spring/issues/79).
- Parent: [Specs #4](https://github.com/marcel615/Ask-Teacher-specs/issues/4). 이번 개발 PR은 Backend #79만 종료하며 Parent 자동 종료 참조를 사용하지 않는다.
- 2026-11-14 현재 대화의 사용자 요청 `ask-teacher 작업 시작하자`에 따라 Main이 유일한 Backend Ready Issue #79를 선정했다. 우선순위 라벨·Project 우선순위는 미설정이고 Blocked by는 비어 있으며 기존 진행 중인 Issue·열린 PR은 없다.
- Main은 2026-11-14 Architect가 제시한 내부 책임 분리 설계와 검증 계획을 승인했다. 공식 외부 계약·인증 정책·인가 규칙은 유지하며 이를 바꾸는 작업은 승인 범위가 아니다.
- #79·Parent #4 본문에는 과거 사용자 결정이 기록돼 있으나 사용자 코멘트 원본은 없다. 해당 본문은 목표·범위의 참고 자료로 사용하며 CI 사용자 확인·비격리 테스트·테스트 생략 등 별도 절차 예외의 승인 근거로 사용하지 않는다. 공통 기본 검증·독립 리뷰·CI 확인·devlog 절차를 따른다.
- 이전 문서정비 PR #75의 merge와 해당 작업의 devlog 생략 승인 기록을 Main이 확인했다. 이전 Issue #58의 devlog 반영도 확인한 뒤 임시 설계를 전환한다.
- 공통 운영 저장소: `ai-dlc-backend`.
- 대상 프로젝트 저장소: `Ask-Teacher-spring`.
- 대상 설정: `Ask-Teacher-spring` 저장소 기준 `ai-dlc.yaml`.
- 브랜치: `feature/issue-79-security-auth-refactor` → `develop`. Main이 fetch 후 HEAD와 origin/develop이 `c6398bf03319f73002b6e01af8c21f221e7438f4`임을 확인하고 작업 브랜치를 생성했다.

## 공식 원본과 확인 버전

- Specs `main` 확인 commit: `06ccca533c1120dcc9696a89c9b2a7e71695369f`.
- [요구사항](https://github.com/marcel615/Ask-Teacher-specs/blob/06ccca533c1120dcc9696a89c9b2a7e71695369f/docs/requirements.md).
- [API·인증 계약](https://github.com/marcel615/Ask-Teacher-specs/blob/06ccca533c1120dcc9696a89c9b2a7e71695369f/docs/api-spec.md).
- [ERD](https://github.com/marcel615/Ask-Teacher-specs/blob/06ccca533c1120dcc9696a89c9b2a7e71695369f/docs/erd.md).
- Architect는 설정된 세 문서를 읽기 전용으로 확인했다. 사용자 ID는 기존 `Long`이며 예시 UUID 사용자 ID를 도입하지 않는다. Backend에는 원문을 복사하지 않는다.

## 목표와 범위

기존 로그인·로그아웃의 실제 처리 책임을 Spring Security 필터·핸들러와 연계하고 토큰 발급 정보·Redis registry·Refresh 쿠키·인증 거부 응답의 역할을 분리한다. JSON API 및 Redis 기반 세션 보안 특성을 유지한다.

### 로그인과 토큰 응답

- `JwtLoginSuccessHandler`는 `AuthenticationSuccessHandler`, `JwtLoginFailureHandler`는 `AuthenticationFailureHandler`를 구현한다.
- JSON 로그인 필터는 정확히 `POST /api/auth/login`을 처리한다. 기존 LoginRequest 파싱·Bean Validation 뒤 기존 AuthenticationManager를 호출하고 성공·실패 핸들러로 연결한다. 폼 입력·리다이렉트·HTTP 세션을 도입하지 않는다.
- 성공 핸들러는 인증된 principal을 이용해 AuthService의 토큰 발급 작업을 호출하고 기존 LoginResponse 및 Refresh 쿠키를 작성한다. 서비스의 자격 검증과 토큰 발급 단계를 구분한다.
- `JwtInformation`은 기존 IssuedTokens의 발급 결과 책임을 대체하는 내부 타입이다. Access 응답과 Refresh 값을 담고 필요하면 기존 Long 사용자 ID를 보유한다. User 엔티티나 비밀번호를 응답하지 않으며 Refresh를 JSON에 추가하지 않는다. 토큰은 toString에서 가린다.
- 입력 오류·깨진 JSON은 400, 잘못된 자격은 401, Redis 장애는 503으로 구분한다. 기존 오류 JSON 형식과 Validation 메시지를 보존한다.

### Registry와 Redis

- `JwtRegistry` 인터페이스와 `RedisJwtRegistry` 구현을 두고 AuthService·JwtAuthenticationFilter는 인터페이스에 의존한다.
- RedisJwtRegistry는 기존 RedisSessionStore의 로그인·회전·세션 활성 확인·가용성 확인·Access/Refresh 무효화 작업에 위임한다.
- 기존 Lua 3개, 실행 인자·반환값, Redis 키 네임스페이스·해시·TTL·원자성·회전 상한을 유지한다. 필요 없는 전체 토큰 검색·수동 만료 청소 API를 추가하지 않는다.
- 기존 StringRedisTemplate 자동 구성을 사용한다. 별도 RedisConfig는 현재 요구사항에 필요하지 않다.

### 쿠키와 로그아웃

- `RefreshTokenCookieProvider`는 Refresh 쿠키 생성·삭제·요청 쿠키 조회를 담당하며 재발급 Controller와 성공 핸들러가 재사용한다.
- 쿠키 이름 refreshToken, 경로 /api/auth, HttpOnly·Secure·SameSite와 기존 설정값, 7일 및 삭제 maxAge를 유지한다.
- `JwtLogoutHandler`는 `LogoutHandler`를 구현하고 기존 verified Access 우선·Refresh 대체 세션 무효화를 호출한다.
- 정확히 POST /api/auth/logout에 대응하는 작은 로그아웃 필터가 핸들러를 호출한다. 성공 시 Refresh 쿠키 삭제·204, Redis 장애 시 기존 JSON 503을 반환한다. 실패 시 성공 응답이나 쿠키 삭제로 덮어쓰지 않는다.
- 이미 만료·로그아웃 상태의 204, CSRF 실패 403·Redis 장애 503 우선 정책을 보존한다.

### SecurityConfig와 Controller

- SecurityConfig에서 로그인 필터·성공/실패 핸들러와 로그아웃 필터·핸들러를 실제로 연결한다.
- CSRF 검사 뒤 로그인 처리, JWT 검증 뒤 로그아웃 처리가 실행되도록 필터 순서를 명시한다. 기본 /logout 처리는 비활성화한다. 필터가 Servlet 컨테이너와 SecurityFilterChain에서 중복 실행되지 않게 한다.
- `AskTeacherAuthenticationEntryPoint`, `AskTeacherAccessDeniedHandler`로 기존 401/403 람다를 분리한다. 상태·JSON·한국어 메시지·UTF-8을 유지하며 필요한 경우 작은 오류 응답 작성기를 공유한다.
- AuthController는 signup·csrf·refresh를 유지하고 필터가 소유하는 login/logout MVC 처리를 제거한다. 재발급은 공통 쿠키 담당 클래스를 사용한다.
- 기존 공개 경로·인가 규칙·CORS·CSRF 범위 및 설정을 유지한다.

## 계약 보존과 대안 검토

- 기본 formLogin은 폼 입력과 리다이렉트가 기존 JSON 계약과 달라 채택하지 않는다. JSON 전용 필터와 실제 핸들러 연결로 목표를 충족한다.
- MVC에 핸들러 클래스를 붙이기만 하는 대안은 SecurityConfig로 처리 구조를 연결한다는 목표가 불분명하므로 채택하지 않는다.
- 표준 LogoutFilter만 사용하는 대안은 JWT 검증 순서와 Redis 예외 응답 처리가 바뀔 수 있다. 명시적 로그아웃 필터에서 LogoutHandler 호출과 기존 실패 정책을 보존한다.
- Redis 저장 구현 재작성이나 타 프로젝트 registry 예시 전체 복제는 불필요한 기능·회귀 위험을 늘리므로 기존 저장소 위임으로 제한한다.
- Access 15분, Refresh 회전마다 최대 7일·최초 로그인부터 최대 30일, 사용자당 단일 세션, 새 로그인·현재 세션 토큰 재사용의 무효화, 과거 세션 토큰의 현재 세션 비간섭, 응답 유실 시 재로그인 및 Redis 장애 시 인증 차단을 유지한다.
- 이 설계에는 공식 요구사항·API·ERD·인증 정책 변경이 필요하지 않다. 구현 중 변경 필요성이 확인되면 기존 계약을 지키는 대안을 먼저 검토·기록하고 영향받는 구현을 보류해 Main에게 보고한다.

## 제외 범위

- 공식 Specs 수정과 Backend 사본의 공식 계약화.
- DB·엔티티 구조, 인증 정책·인가 규칙, JWT 키·비밀값·중요 환경 설정 변경.
- Frontend 구현·React 자동 재발급·탭 간 조정, 운영 Redis 복구·고가용성·배포 변경.
- Redis 기능 중복 구현과 기존 동시성·TTL 정책 변경.
- AGENTS.md·Skill·Agent 정의·ai-dlc.yaml·빠른 지시문·리뷰 기준·템플릿 등 AI-DLC 운영 파일 변경.
- Parent 자동 종료와 사용자 merge 대행.

## 검증 계획

- 기존 Controller 로그인·로그아웃 검증을 대응하는 필터/핸들러 테스트로 옮기되 검증 범위를 줄이지 않는다. 남는 Controller의 signup·csrf·refresh 및 Service 동작을 유지한다.
- 입력 누락·빈 값·잘못된 이메일·깨진 JSON, 성공 JSON·쿠키, 잘못된 자격, Redis 장애를 검증한다.
- 실제 Security 체인에서 CSRF 없는 로그인·로그아웃의 403 우선, CORS, 비대상 HTTP 메서드 처리, 쿠키 삭제·장애 정책과 JWT 로그아웃 식별을 확인한다.
- AuthSessionIntegrationTest·AuthRedisFailureIntegrationTest 및 기존 JWT·CSRF·SecurityConfig 검증을 유지·보완한다. 새 로그인·회전·재사용·동시 요청·세션 교체·로그아웃 경합을 확인한다.
- test 프로필의 격리된 H2·테스트 Redis(기존 TEST_REDIS_PORT, 기본 16379)·임시 업로드 경로를 사용한다. 개발·운영 데이터나 파일을 사용하지 않으며 실행한 임시 자원을 정리한다.
- Windows 기본 명령 `.\gradlew.bat test`를 실제 실행하고 환경·테스트 수·실패·건너뜀·미검증을 기록한다. 새 테스트는 의미별 @Nested와 한국어 @DisplayName을 사용한다.
- diff에서 Lua·TTL·Redis 키·인가 순서·JSON/쿠키 계약 보존과 보호 파일 변경 없음을 확인하고 git diff --check를 통과한다.

## 완료 조건과 게이트

- 승인된 역할별 클래스가 실제 요청 처리에 연결되고 공식 계약·인증 정책을 유지한다. 생략·정책 변경이 필요하면 구현 완료로 간주하지 않고 Main에게 보고한다.
- Main의 구현 게이트에서 설계와 diff 일치 및 관련·전체 테스트 통과를 확인한다.
- Architect가 공통 리뷰 기준으로 독립 리뷰하고 필수 지적을 해결·재검증한다. AI 리뷰는 필수 사람 승인을 대신하지 않는다.
- Reporter의 근거 기반 devlog를 append-only로 반영한다. PR에 변경 commit·공식 계약 SHA·실행 환경·테스트 결과·미검증·소비자 영향 판단을 기록한다.
- feature 브랜치에서 develop으로 PR을 생성하며 Backend #79만 종료하도록 한다.
- 최종 head의 CI·필수 리뷰·보호 조건·충돌·devlog를 확인한다. 미확인·대기·실패는 준비 완료로 보고하지 않으며 merge는 사용자가 직접 한다.
