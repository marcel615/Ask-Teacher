# Current Work Log

## Issue #34 — CI 구축 및 Codecov 커버리지 연동

- 승인된 Architect 사전 문서인 `docs/current-task.md` 변경을 보존했다.
- `origin/develop` fetch 후 `develop`과의 차이가 0/0임을 확인하고 `feature/issue-34-ci-codecov`를 생성했다.
- 최초 fetch는 샌드박스의 `.git` 쓰기 제한으로 실패했으며, 승인된 권한으로 재실행하여 성공했다.
- 기존 Issue #33 임시 기록을 현재 Issue 기준으로 초기화했다.

## 주요 결정 및 중간 검증

- 공식 문서에서 확인한 현재 안정 메이저 버전에 맞춰 `actions/checkout@v7`, `actions/setup-java@v6`, `gradle/actions/setup-gradle@v6`, `codecov/codecov-action@v7`을 사용했다.
- workflow 권한은 저장소 checkout에 필요한 `contents: read`만 부여했다.
- Codecov 업로드 전에 JaCoCo XML 파일 존재를 명시적으로 검사하고, 업로드 오류는 `fail_ci_if_error: true`로 CI 실패 처리한다.

## 테스트 및 미검증 사항

- 최초 `.\gradlew.bat test`는 Gradle 9.4.1 배포본 다운로드가 샌드박스 네트워크 제한으로 실패했다. 승인된 네트워크 권한으로 재실행하여 성공했다.
- `.\gradlew.bat test`: 105개 실행, 실패 0, 오류 0, skip 0, `BUILD SUCCESSFUL`.
- `test` 완료 후 `jacocoTestReport`가 실행되었고 XML은 `build/reports/jacoco/test/jacocoTestReport.xml`, HTML은 `build/reports/jacoco/test/html/index.html`에 생성됐다.
- 신규 또는 변경 API가 없어 Controller, Service, Repository 및 API 통합 테스트를 추가하지 않았으며 기존 105개 테스트를 그대로 실행했다.
- workflow의 실행 조건(`develop` 대상 PR, `main` push), Java 17, Gradle Wrapper 명령, XML 경로, `CODECOV_TOKEN`, 업로드 실패 처리를 정적으로 확인했다.
- 로컬 환경에 `actionlint` 또는 별도 YAML 파서가 없어 GitHub Actions 전용 스키마 검증은 수행하지 못했다.
- PR #37의 최초 CI는 `./gradlew: Permission denied`와 종료 코드 126으로 테스트 시작 전에 실패했다. Git에 기록된 `gradlew` 모드가 `100644`인 것이 원인이었다.
- workflow에 별도 `chmod` 단계를 추가하지 않고 `git update-index --chmod=+x gradlew`로 실행 비트를 `100755`로 기록했다.
- 수정 후 GitHub Actions run `35093686170`에서 Gradle 테스트, `jacocoTestReport`, JaCoCo XML 파일 확인 및 Codecov 업로드 단계가 모두 성공했고 전체 job이 통과했다.
- `CODECOV_TOKEN`이 적용된 Codecov 업로드 성공을 확인했다. `main` push trigger와 README 배지의 기준 브랜치 및 실제 표시는 아직 미검증이다.
