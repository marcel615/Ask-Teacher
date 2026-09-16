# Current Task

## Issue

- Issue: #34
- Title: feat: CI 구축 및 Codecov 커버리지 연동
- URL: https://github.com/marcel615/Ask-Teacher/issues/34
- State: OPEN
- Labels: 없음

## 목표

`develop` 대상 Pull Request와 `main` 브랜치 push마다 자동으로
Gradle 테스트를 실행한다.

JaCoCo로 XML 커버리지 리포트를 생성하고 Codecov에 업로드하여,
README에서 `develop` 브랜치의 커버리지를 확인할 수 있도록 한다.

커버리지 기준값은 적용하지 않으며 커버리지 수치가 낮다는 이유로
CI를 실패시키지 않는다.

## 범위

- GitHub Actions CI workflow 신규 작성
- `develop` 대상 Pull Request에서 CI 실행
- `main` 브랜치 push에서 CI 실행
- Java 17 환경 구성
- Gradle Wrapper를 사용한 `./gradlew test` 실행
- JaCoCo 플러그인 적용
- `./gradlew test` 실행 시 JaCoCo XML 리포트 생성
- 생성된 JaCoCo XML 리포트를 Codecov에 업로드
- GitHub Actions Secret `CODECOV_TOKEN` 사용
- Codecov 업로드 오류 발생 시 CI 실패
- README에 `develop` 브랜치 기준 Codecov 배지 추가
- 테스트 및 커버리지 실행 결과와 미검증 사항 기록

## 제외 범위

- 최소 커버리지 기준 설정
- 커버리지 수치 또는 감소율에 따른 CI 실패 처리
- `jacocoTestCoverageVerification` 기준 추가
- `codecov.yml`을 이용한 별도 커버리지 정책 구성
- `main` 브랜치 기준 README 커버리지 배지 추가
- 배포 자동화(CD)
- Docker 이미지 빌드 및 배포
- 정적 분석 및 코드 포맷 검사
- Dependabot 설정
- GitHub 브랜치 보호 규칙 설정
- API, Entity 및 운영 DB 구조 변경
- 기존 테스트의 기대 결과 변경 또는 테스트 비활성화
- CI 구축과 직접 관련 없는 애플리케이션 코드 수정
- 기존 기능 버그 수정

기존 테스트 실패나 명세 불일치를 발견하면 기대값 변경이나
테스트 비활성화로 숨기지 않는다.

실패한 테스트와 CI에 미치는 영향을 사용자에게 보고하고,
별도 수정이 필요하면 범위를 먼저 협의한다.

## 요구사항 변경 요약

- docs/requirements.md 변경 없음
- 사유:
  - 이번 Issue는 사용자 기능이나 비즈니스 규칙을 변경하지 않는다.
  - 테스트 자동 실행과 커버리지 수집을 위한 개발 인프라 작업이다.
- 참조: docs/requirements.md

## API 변경 요약

- docs/api-spec.md 변경 없음
- 신규 API 또는 기존 API 계약 변경 없음
- 요청·응답 DTO, Validation, 상태 코드 및 오류 응답 변경 없음
- 참조: docs/api-spec.md

## ERD 변경

- ERD 변경 없음
- 사유:
  - JaCoCo, GitHub Actions 및 Codecov 연동만 추가한다.
  - Entity, 운영 DB 테이블, 컬럼, 관계 및 제약조건을 변경하지 않는다.
- 참조: docs/erd.md

## CI 설계

### 실행 조건

GitHub Actions workflow는 다음 이벤트에서 실행한다.

- `pull_request`
  - 대상 브랜치: `develop`
- `push`
  - 대상 브랜치: `main`

그 밖의 브랜치 push와 `main` 대상 Pull Request는 이번 Issue의
자동 실행 대상에 포함하지 않는다.

### 실행 환경

- GitHub-hosted Ubuntu runner 사용
- Java 17 사용
- Gradle Wrapper 사용
- 애플리케이션 서버를 별도로 기동하지 않음
- 테스트는 기존 테스트 프로필과 격리된 테스트 환경을 사용

### 실행 순서

1. 저장소 코드를 checkout한다.
2. Java 17 환경을 구성한다.
3. Gradle 실행 환경을 구성한다.
4. `./gradlew test`를 실행한다.
5. JaCoCo XML 리포트 생성 여부를 확인한다.
6. 생성된 XML 리포트를 Codecov에 업로드한다.

테스트가 실패하면 Gradle 명령과 GitHub Actions 작업도 실패해야 하며,
Codecov 업로드 단계는 실행하지 않는다.

### JaCoCo 설정

- `build.gradle`에 JaCoCo 플러그인을 적용한다.
- 기존 `test` 작업의 JUnit Platform 설정을 유지한다.
- `test` 완료 후 `jacocoTestReport`가 실행되도록 연결한다.
- `./gradlew test` 한 번으로 테스트와 리포트 생성을 완료해야 한다.
- Codecov 업로드용 XML 리포트를 활성화한다.
- HTML 리포트도 로컬 확인용으로 생성한다.
- 커버리지 검증 규칙은 추가하지 않는다.
- 기존 테스트 코드나 테스트 대상을 커버리지 수치를 높이기 위해 변경하지 않는다.

예상 XML 리포트 경로:

`build/reports/jacoco/test/jacocoTestReport.xml`

실제 경로가 다르면 Builder가 설정과 생성 결과를 확인하고
workflow의 업로드 경로를 동일하게 맞춘다.

### Codecov 설정

- 공식 Codecov GitHub Action을 사용한다.
- 구현 시점에 지원되는 안정 버전을 사용하고 버전을 명시한다.
- 업로드 파일은 JaCoCo XML 리포트 경로로 명시한다.
- 인증에는 `${{ secrets.CODECOV_TOKEN }}`을 사용한다.
- 업로드 오류가 발생하면 workflow가 실패하도록 설정한다.
- 커버리지 수치 미달에 따른 실패 정책은 설정하지 않는다.
- Codecov 토큰 값을 workflow나 저장소 파일에 직접 작성하지 않는다.

`CODECOV_TOKEN`은 사용자가 Codecov에서 발급받아 GitHub 저장소의
Actions repository secret으로 등록한다.

Secret 등록과 실제 Codecov 서비스 연결은 저장소 외부 설정이므로
Builder의 파일 변경 대상에는 포함하지 않는다.

### README 배지

README 상단의 프로젝트 제목 인근에 Codecov 배지를 추가한다.

- Repository: `marcel615/Ask-Teacher`
- 기준 브랜치: `develop`
- 배지 클릭 시 해당 저장소의 Codecov 페이지로 이동
- `main` 브랜치 배지는 추가하지 않음

## 보안 및 제약

- workflow에는 필요한 최소 권한만 부여한다.
- `CODECOV_TOKEN`을 로그나 파일에 출력하지 않는다.
- GitHub Actions가 Secret 값을 마스킹하더라도 출력 명령을 추가하지 않는다.
- 외부 fork에서 생성된 Pull Request에는 저장소 Secret이 전달되지 않을 수 있다.
- 현재 작업은 저장소 내부 브랜치에서 생성한 Pull Request를 기본 대상으로 한다.
- 외부 fork Pull Request 지원이 필요하면 토큰 없는 업로드 또는
  업로드 단계 조건 처리를 별도로 협의한다.

## 테스트 및 검증

### 로컬 검증

Windows PowerShell에서는 다음 명령을 실행한다.

`.\gradlew.bat test`

다음 항목을 확인한다.

- 전체 테스트 통과
- 테스트 실패 시 Gradle 명령 실패
- JaCoCo XML 리포트 생성
- JaCoCo HTML 리포트 생성
- 기존 테스트 수와 검증 범위 유지
- 커버리지 기준 미달 검증 작업이 추가되지 않음

### 정적 설정 검토

- workflow YAML 문법 확인
- `develop` 대상 Pull Request trigger 확인
- `main` push trigger 확인
- Java 17 설정 확인
- Gradle Wrapper 명령 확인
- Codecov XML 경로 확인
- `CODECOV_TOKEN` 참조 확인
- 업로드 오류 실패 설정 확인
- README 배지의 저장소와 `develop` 브랜치 확인

### 원격 검증

실제 GitHub Actions와 Codecov 동작은 사용자가 확인한다.

- `develop` 대상 Pull Request에서 workflow 실행
- `main` push에서 workflow 실행
- 테스트 결과 반영
- Codecov 업로드 성공
- README 배지에서 `develop` 커버리지 표시

Builder는 실제 원격 실행을 확인하지 못한 경우 이를 완료로 간주하지 않고
미검증 사항으로 작업 기록과 PR 요약에 남긴다.

## 예상 변경 파일

### Architect 사전 반영 문서

- docs/current-task.md
  - Issue #34의 확정된 목표, 범위, 설계 및 완료 조건 반영

이번 초안 승인 후 Architect가 반영하는 문서다.

다음 문서는 실제 변경이 필요하지 않아 포함하지 않는다.

- docs/requirements.md
- docs/api-spec.md
- docs/erd.md

### Builder 구현 변경 예상 파일

- .github/workflows/ci.yml
  - GitHub Actions CI workflow 신규 작성
  - `develop` 대상 Pull Request 및 `main` push trigger 설정
  - Java 17, Gradle 테스트, Codecov 업로드 구성

- build.gradle
  - JaCoCo 플러그인 적용
  - `test`와 `jacocoTestReport` 연계
  - XML 및 HTML 커버리지 리포트 설정

- README.md
  - `develop` 브랜치 기준 Codecov 배지 추가

- docs/current-work-log.md
  - Issue #34 기준으로 초기화
  - 구현 결정, 테스트 결과, 리포트 경로 및 미검증 사항 기록

위 파일 외의 변경이 필요하면 Builder가 변경 사유와 범위를 먼저 보고하고
사용자 승인을 받은 후 진행한다.

## 외부 설정

파일 변경과 별도로 다음 설정이 필요하다.

- Codecov에 `marcel615/Ask-Teacher` 저장소 연결
- Codecov repository upload token 발급
- GitHub Actions repository secret `CODECOV_TOKEN` 등록

Secret 등록은 사용자가 수행한다.

## 구현 및 검증 순서

1. `build.gradle`에 JaCoCo를 적용한다.
2. `./gradlew test` 실행 시 XML 및 HTML 리포트가 생성되도록 설정한다.
3. 로컬에서 전체 테스트와 리포트 생성을 검증한다.
4. `.github/workflows/ci.yml`을 작성한다.
5. workflow의 trigger, Java 버전, 명령 및 업로드 경로를 검토한다.
6. README에 `develop` 브랜치 기준 Codecov 배지를 추가한다.
7. 전체 테스트를 다시 실행한다.
8. 테스트 결과와 미검증 원격 항목을 `docs/current-work-log.md`에 기록한다.
9. 사용자가 실제 GitHub Actions 실행과 Codecov 반영 결과를 확인한다.

## 완료 조건

- [ ] `develop` 대상 Pull Request에서 CI가 실행되도록 구성되어 있다.
- [ ] `main` 브랜치 push에서 CI가 실행되도록 구성되어 있다.
- [ ] Java 17과 Gradle Wrapper를 사용한다.
- [ ] CI에서 `./gradlew test`를 실행한다.
- [ ] 테스트 실패 시 CI 작업도 실패한다.
- [ ] `./gradlew test` 실행으로 JaCoCo XML 리포트가 생성된다.
- [ ] JaCoCo HTML 리포트가 생성된다.
- [ ] Codecov 업로드 대상 XML 경로가 실제 생성 경로와 일치한다.
- [ ] Codecov 업로드에 `CODECOV_TOKEN`을 사용한다.
- [ ] Codecov 업로드 오류 시 CI가 실패하도록 구성되어 있다.
- [ ] 최소 커버리지 기준이나 미달 실패 정책이 적용되지 않았다.
- [ ] README에 `develop` 브랜치 기준 Codecov 배지가 추가되었다.
- [ ] 로컬 전체 테스트가 통과했다.
- [ ] 기존 테스트 범위와 기대 결과를 임의로 변경하지 않았다.
- [ ] 테스트 결과, 커버리지 리포트 경로 및 미검증 사항을 기록했다.
- [ ] 사용자가 실제 GitHub Actions 실행 결과를 확인했다.
- [ ] 사용자가 Codecov 업로드 및 README 배지 반영 결과를 확인했다.
