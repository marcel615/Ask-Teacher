# AGENTS.md

## 핵심 규칙

- 사용자는 PM이자 최종 승인자다. 한 번에 하나의 Issue만 진행한다.
- Issue 작성·확장은 `.github/ISSUE_TEMPLATE`을 참고하고, 요구사항이 불명확하면 사용자에게 확인한다.
- 구현 기준은 승인된 `docs/current-task.md`이며 범위 밖은 구현하지 않는다. 사용자가 특정 작업에서 이 절차를 생략하도록 지시한 경우 그 승인 범위를 따른다.
- GitHub Issue 생성·수정, 파일 수정, 브랜치 생성·이동, git add·commit·push, PR 생성, 삭제, DB·인증·인가 변경은 사용자 승인 후 진행한다.
- 예외: 이번 Issue의 feature 브랜치는 PR merge와 미반영 커밋·변경이 없음을 확인한 경우 별도 승인 없이 정리할 수 있다. 확인할 수 없으면 중단한다. 실행 환경의 권한 정책은 별도로 따른다.
- 최소한의 변경만 하고 기존 프로젝트 스타일을 따른다.

## 브랜치

- 기준 브랜치: `develop`
- 작업 브랜치: `feature/issue-번호-작업명`
- PR 방향: `feature/*` → `develop`
- `main`이나 `develop`에서 직접 구현하지 않는다.

## 참고 문서

- 상세 워크플로우: `.agents/skills/issue-driven-dev/SKILL.md`
- 빠른 지시문: `docs/prompts/quick.md`
- Issue 템플릿: `.github/ISSUE_TEMPLATE/feature_request.yml`
- 요구사항·API·ERD: `docs/requirements.md`, `docs/api-spec.md`, `docs/erd.md`
- 리뷰 기준: `docs/review-checklist.md`
- 임시 작업 기록: `docs/current-work-log.md`
- 영구 작업 기록: `docs/devlog/YYYY-MM.md`
- PR 템플릿: `.github/PULL_REQUEST_TEMPLATE/pull_request_template.md`

## 테스트와 기록

- 기본 검증은 `./gradlew test`, Windows PowerShell에서는 `.\gradlew.bat test`다.
- 새 API 또는 변경된 API는 관련 Controller·Service·Repository 테스트와 통합 테스트를 작성·보완한다. 요청·검증·응답·예외, 비즈니스 규칙, 실제 쿼리와 인증부터 DB까지 주요 흐름을 검증한다.
- 테스트 DB, 인증 설정과 파일 경로를 개발·운영 환경에서 격리하고 데이터와 파일을 정리한다.
- 테스트 범위, 실행 결과와 미검증 사항을 PR 또는 devlog에 기록한다. 기존 버그나 명세 불일치를 기대값 변경이나 테스트 비활성화로 숨기지 않고 보고한다.
- Issue 완료 전에 devlog 반영을 확인한다. 새 Issue의 임시 작업 기록을 초기화하기 전 이전 Issue의 devlog 반영을 확인하고, 불명확하면 중단한다.

## 작업 효율과 보고

- 변경되지 않았음을 확인할 수 있는 기존 조사 결과는 재사용하고, 상태가 불확실하면 다시 확인한다.
- 승인 범위를 유지하되 상황이 바뀌거나 실행 전제가 틀리면 중단하고 보고한다.
- 코드 탐색과 리뷰는 변경 파일과 diff부터 시작하고 필요한 문맥만 추가로 읽는다.
- 승인 필요한 다음 단계의 변경을 미리 수행하지 않는다.
- 변경 내용, 검증 결과와 남은 문제를 간결히 보고한다.
