# Current Task

## Issue

- Issue: [#40 Docker Desktop PostgreSQL 개발 DB 연결](https://github.com/marcel615/Ask-Teacher/issues/40)
- 우선순위: medium; 확인된 열린 선행 Issue 없음.

## 목표와 사용자 결정

- Docker Desktop의 로컬 PostgreSQL에 애플리케이션을 연결하고 실행 방법을 문서화한다.
- 별도 `application-dev` 설정을 사용한다. 출처: marcel615의 [Issue 코멘트](https://github.com/marcel615/Ask-Teacher/issues/40#issuecomment-6723037052).
- 개발 DB의 `ddl-auto`는 `update`로 한다. 출처: 이번 대화에서 사용자가 직접 선택했다. 이 설정은 시작 시 대상 DB의 스키마를 변경할 수 있으므로 로컬 개발에 한정한다.
- DB 비밀번호와 다른 연결 환경변수는 Git에서 제외된 로컬 `.env`에 보관하고 예시 파일을 제공한다. 출처: 이번 대화에서 사용자가 직접 결정했다.

## 범위

- PostgreSQL JDBC 드라이버와 로컬 `dev` 프로필의 DB 연결 설정을 추가한다.
- DB 연결 정보는 로컬 `.env`와 환경변수로 전달하고 저장소와 문서에 실제 비밀값을 기록하지 않는다. Git 제외와 예시 파일을 확인한다.
- `dev`에서 H2 콘솔을 비활성화하고 기존 기본 및 테스트 설정의 격리를 유지한다.
- 사용자 요청에 따라 Docker Compose 파일과 짧은 실행 명령으로 로컬 PostgreSQL 컨테이너를 관리한다.
- Docker Desktop에서의 PostgreSQL 실행·연결·문제 확인 방법과 `update`의 적용 범위를 README에 설명한다.
- 테스트는 기존 사용자 DB가 아닌 격리된 임시 PostgreSQL DB를 사용하고 정리한다.

## 제외 범위

- 운영 PostgreSQL 배포·설정, 인증·인가 및 API 동작 변경.
- 별도로 승인되지 않은 Entity 및 DB 구조 변경. 단, 격리된 임시 DB에서 기존 Entity에 따른 스키마 생성은 연결 검증에 포함한다.
- 이번 Issue와 관련 없는 문서·코드 정비.

## 요구사항·API·ERD

- `docs/requirements.md`: 사용자 기능의 변경이 없어 변경하지 않는다.
- `docs/api-spec.md`: API 계약의 변경이 없어 변경하지 않는다.
- `docs/erd.md`: Entity와 승인된 DB 구조의 변경이 없어 변경하지 않는다.

## 검증 계획

- 격리된 새 PostgreSQL 컨테이너에서 애플리케이션 기동과 연결을 확인하고, 재기동 시 데이터 유지와 잘못된 연결 설정의 실패를 확인한다.
- 테스트 프로필이 개발 DB와 격리되는지 확인하고 `.\gradlew.bat test`를 실행한다.
- 설정과 문서의 일치, 민감값 미포함 및 `git diff --check`를 확인한다.
- 검증에 사용한 임시 데이터와 리소스를 정리하고 결과와 미검증 사항을 기록한다.
- Docker 엔진은 설계 시점에 연결되지 않았다. 실제 연결 검증은 엔진 실행 후 수행하며, 수행할 수 없으면 미검증으로 보고한다.

## 예상 변경 파일과 완료 조건

- `build.gradle`, `src/main/resources/application-dev.yaml`, `compose.yaml`, `.env.example`, `run-dev.ps1`, `.gitignore`, `README.md`: 드라이버·프로필·Compose 실행 설정과 안내.
- `docs/current-work-log.md`, 월별 devlog: 작업 및 검증 기록.
- 로컬 PostgreSQL 연결과 실패 조건, 테스트 격리 및 전체 테스트를 검증하고 PR 리뷰와 devlog를 반영한다.
- 최종 push의 정확한 head에 대해 CI·리뷰·보호 조건과 충돌 여부를 확인한다.
