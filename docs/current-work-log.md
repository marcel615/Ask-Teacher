# Current Work Log

## Issue #58 — Refresh Token 기반 인증 재발급

- 이전 Issue #65의 PR #71 머지와 devlog 반영을 확인했다. `feature/issue-58-refresh-session`에서 작업한다.
- 사용자 결정: GitHub Issue #58 사용자 댓글과 2026-11-09 대화에 따라 토큰 재사용 즉시 차단, React 요청 조정은 후속 작업, 로그아웃의 CSRF 403·Redis 장애 503 우선, 단일 개발 Redis와 운영 복구 정책 제외를 적용한다. 사용자가 상세 설계 및 구현을 승인했다.
- Project In Progress로 변경했다. 설계 문서 반영과 구현·검증·리뷰가 남았다.
- 인증·세션 구현: Redis 원자 회전/재사용 감지와 단일 세션, JWT 세션 확인, CSRF 서명·Refresh 쿠키 결합, CORS 및 쿠키 정책을 반영했다. CSRF 전용 비밀값은 환경 변수로만 받으며 JWT 키는 사용자 추가 승인에 따라 필수 환경 변수로 전환했으며 실제 .env는 변경하지 않았다.
- 격리 Redis와 H2 환경에서 재사용·동시 회전·세션 교체·만료·CSRF·로그아웃·실제 HTTP 쿠키·장애 및 기존 회귀를 확인했다. Corretto 17에서 `./gradlew.bat test --rerun-tasks` 성공: 155건, 실패·오류·건너뜀 0건.
- 단일 메모리 Redis 외 운영 복구·고가용성, React 자동 재발급·탭 간 조정은 미구현이다. 기존 공개 JWT 기본 키는 사용자 승인으로 제거했으며 키 변경 후 재로그인이 필요하다. Architect 리뷰와 CI 검증이 남았다.
- 독립 리뷰 필수 수정 1회: JWT를 한 번 검증한 결과를 필터와 로그아웃에서 공유해 만료 경계 재파싱을 제거했다. 실제 Redis 연결 장애 시 HTTP 503과 CSRF 403 우선, Access·Refresh 로그아웃 후 거부, 새 로그인과 두 방식 로그아웃의 경합을 검증했다.
- 사용자 추가 승인으로 공개 JWT 기본 키를 제거하고 필수 JWT_SECRET과 약한 키 거부를 적용했다. 서명 키 교체 시 기존 Refresh 세션도 거부하며 재로그인이 필요하다. 이전 키로 TTL 내 되돌릴 때의 세션 부활 방지는 운영 복구 정책의 별도 범위로 기록했다.
- 리뷰 수정 후 Corretto 17 `./gradlew.bat test --rerun-tasks` 성공: 164건, 실패·오류·건너뜀 0건. Architect 재검토 및 최종 head CI 검증은 남았다.
