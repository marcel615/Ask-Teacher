# Current Work Log

## Issue #40 — Docker Desktop PostgreSQL 개발 DB 연결

- 이전 Issue #53의 PR #54 머지와 `docs/devlog/2026-11.md` 반영을 Main이 확인했다.
- `feature/issue-40-local-postgresql`에서 승인된 설계를 적용했다. `dev` 프로필만 환경변수 기반 PostgreSQL 및 `ddl-auto: update`를 사용하고 H2 콘솔을 끈다. 사용자 결정은 `docs/current-task.md`에 기록했다.
- README에 로컬 Docker Desktop 실행·재사용·연결 실패 확인과 스키마 변경 주의사항을 추가했다.
- 사용자 후속 요청에 따라 Compose 실행과 `.env` 기반 로컬 DB 설정을 추가했다. 기존 `.env`가 없음을 확인하고 Git에서 제외되는 로컬 파일을 생성했으며, 비밀값 없는 `.env.example`을 제공한다. 기존 개발 볼륨은 변경하지 않았다.

## 검증 및 미확인 사항

- Corretto 17을 사용해 `.\gradlew.bat test` 실행: 성공 (6개 작업 중 3개 실행). 테스트 프로필은 별도 H2 메모리 DB를 사용한다.
- `.\gradlew.bat bootJar` 성공. 새 격리 PostgreSQL 17 컨테이너에서 `dev` 애플리케이션 기동과 DB 연결을 확인하고, 데이터를 변경한 후 재기동해 유지됨을 확인했다. 잘못된 포트에서 기동 실패도 확인했다. 임시 컨테이너와 데이터·로그를 정리했다.
- 기본 설정은 기존 H2 `create`, 테스트 설정은 H2 `create-drop`으로 유지했다. 실제 사용자 DB는 사용하지 않았다.
- Compose 변경 후 `.\gradlew.bat test bootJar` 성공 (8개 작업 중 5개 실행). 격리된 Compose 프로젝트·포트·볼륨과 별도 `.env`에서 PostgreSQL 연결과 컨테이너 및 앱 재시작 후 데이터 유지를 확인했다. 검증용 리소스를 정리했다.
- 새 임시 볼륨을 Docker로 미리 생성한 뒤 Compose가 기존 이름의 볼륨을 사용하는 것도 확인하고 해당 임시 리소스를 정리했다. `.env`가 Git에서 제외되고 `.env.example`은 추적 가능함을 확인했다.
- 독립 리뷰 후 Compose raw env_file과 PowerShell 실행 스크립트가 `.env` 값을 그대로 전달하도록 변경했다. `$`, 백슬래시와 `=`가 포함된 임시 비밀번호로 격리 PostgreSQL의 실제 앱 인증을 확인했고, 기존 환경변수 복원·빈 비밀번호와 파일 누락 실패·README의 준비 상태 확인 명령을 검증했다. `.\gradlew.bat test bootJar`를 다시 실행해 성공했다 (8개 작업 중 5개 실행). 임시 리소스는 정리했다.
- PR #63 생성 및 Project In Review 확인. Architect 독립 리뷰에서 필수 수정 없음. 최종 head CI와 보호 조건은 확인 대기 중이다.
