# AGENTS.md

## 핵심 규칙

- 사용자는 PM이자 최종 승인자다. 한 번에 하나의 Issue만 진행한다.
- Issue 작성·확장은 `.github/ISSUE_TEMPLATE`을 참고하고, 요구사항이 불명확하면 사용자에게 확인한다.
- 구현 기준은 Main이 품질 게이트로 승인한 `docs/current-task.md`이며 범위 밖은 구현하지 않는다. 사용자가 특정 작업에서 이 절차를 생략하도록 지시한 경우 그 승인 범위를 따른다.
- 사용자가 작업 시작을 지시하면 Main은 지정된 Issue 또는 선정 기준에 따라 선택한 Issue 하나를 품질 게이트와 아래 예외를 지켜 설계·구현·검증하고 PR을 생성한다.
- Main은 명확한 기술 후속 작업과 사용자가 승인한 기능 후보를 사이클당 최대 2개의 Issue로 생성할 수 있다. 미결정 사항이 있으면 Issue에 명시하고 `needs: decision` 라벨을 붙여 선정에서 제외한다. 새로운 기능 아이디어는 사용자가 승인하기 전에는 Issue로 만들지 않는다.
- 사용자 코멘트의 결정이 명확하면 `needs: decision`을 해제한다. 미결정 Issue는 자동 선정에서 제외하고, 사용자가 직접 지정하면 질문한다. 선정된 Issue의 결정 내용과 출처를 Architect에게 전달해 설계에 반영한다. 그 외 Issue 생성·수정은 사용자 승인 후 진행한다. PR merge는 사용자가 직접 한다.
- 요구사항이 불명확하거나 Issue 범위 확장, DB 구조·인증·인가 변경, 비밀값·API 키·중요 환경 설정 변경, 위험 작업 또는 사용자·팀의 필수 승인이 필요하면 해당 작업을 중단하고 사용자에게 확인한다. 비밀값은 출력하거나 기록하지 않는다.
- 테스트·CI·리뷰 실패가 수정 한도 내에 해결되지 않거나 근거·권한을 확인할 수 없으면 중단하고 보고한다. 실행 환경의 승인 및 GitHub 보호 규칙은 그대로 따른다.
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
- 기능 아이디어와 승인: `docs/feature-ideas.md`
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
