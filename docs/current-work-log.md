# Current Work Log

## Issue #40 — Docker Desktop PostgreSQL 개발 DB 연결

- 이전 Issue #53의 PR #54 머지와 `docs/devlog/2026-11.md` 반영을 Main이 확인했다.
- `feature/issue-40-local-postgresql`에서 승인된 설계를 적용했다. `dev` 프로필만 환경변수 기반 PostgreSQL 및 `ddl-auto: update`를 사용하고 H2 콘솔을 끈다. 사용자 결정은 `docs/current-task.md`에 기록했다.
- README에 로컬 Docker Desktop 실행·재사용·연결 실패 확인과 스키마 변경 주의사항을 추가했다.

## 검증 및 미확인 사항

- Corretto 17을 사용해 `.\gradlew.bat test` 실행: 성공 (6개 작업 중 3개 실행). 테스트 프로필은 별도 H2 메모리 DB를 사용한다.
- `.\gradlew.bat bootJar` 성공. 새 격리 PostgreSQL 17 컨테이너에서 `dev` 애플리케이션 기동과 DB 연결을 확인하고, 데이터를 변경한 후 재기동해 유지됨을 확인했다. 잘못된 포트에서 기동 실패도 확인했다. 임시 컨테이너와 데이터·로그를 정리했다.
- 기본 설정은 기존 H2 `create`, 테스트 설정은 H2 `create-drop`으로 유지했다. 실제 사용자 DB는 사용하지 않았다.
- PR #63 생성 및 Project In Review 확인. Architect 독립 리뷰에서 필수 수정 없음. 최종 head CI와 보호 조건은 확인 대기 중이다.
