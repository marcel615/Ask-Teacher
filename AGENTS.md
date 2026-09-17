# AGENTS.md

## 핵심 규칙

- 사용자는 PM이자 최종 승인자다.
- 한 번에 하나의 Issue만 진행한다.
- Issue 작성/확장 시 Architect는 `.github/ISSUE_TEMPLATE`을 기준으로 사용자와 요구사항을 협의한다.
- 요구사항이 불명확하면 Architect가 임의로 확정하지 않고 사용자에게 필요한 사항을 확인한다.
- 구현 기준은 `docs/current-task.md`다.
- `docs/current-task.md` 범위 밖은 구현하지 않는다.
- GitHub Issue 생성/수정, 파일 수정, 브랜치 생성/이동, git add/commit/push, PR 생성, 삭제, DB/인증/인가 변경은 사용자 승인 후 진행한다.

## 브랜치

- 기준 브랜치: `develop`
- 작업 브랜치: `feature/issue-번호-작업명`
- PR 방향: `feature/*` → `develop`
- `main`이나 `develop`에서 직접 구현하지 않는다.

## 참고 문서

- 상세 워크플로우: `.agents/skills/issue-driven-dev/SKILL.md`
- 빠른 지시문: `docs/prompts/quick.md`
- Issue 템플릿: `.github/ISSUE_TEMPLATE/feature_request.yml`
- 요구사항: `docs/requirements.md`
- API 명세: `docs/api-spec.md`
- ERD: `docs/erd.md`
- 리뷰 기준: `docs/review-checklist.md`
- 임시 구현 기록: `docs/current-work-log.md`
- 영구 작업 기록: `docs/devlog/YYYY-MM.md`
- PR 템플릿: `.github/PULL_REQUEST_TEMPLATE/pull_request_template.md`

## 테스트

```bash
./gradlew test
```

## 자동 API 테스트

- 새 API 또는 변경된 API는 관련 Controller, Service, Repository 테스트와 통합 테스트를 작성·보완한다.
- Controller는 요청 바인딩·Validation·응답·예외 처리를, Service는 비즈니스 규칙을, Repository는 테스트 DB에서 실제 쿼리와 매핑을 검증한다.
- 통합 테스트는 인증을 포함한 HTTP 요청부터 DB 처리까지 정상·주요 실패 흐름을 검증한다.
- 테스트 DB, 인증 설정, 파일 저장 경로를 개발·운영 환경과 격리하고 데이터와 파일을 정리한다.
- 기본 검증은 `./gradlew test`다. Windows PowerShell에서는 `.\gradlew.bat test`를 실행할 수 있다.
- 테스트 범위, 실행 결과, 미검증 사항을 PR 요약 또는 devlog에 기록한다.
- 기존 버그나 명세 불일치는 기대값 변경 또는 테스트 비활성화로 숨기지 않고 사용자에게 보고한다.

## 작업 효율 규칙

- 같은 세션에서 이미 확인했고 이후 변경되지 않은 문서나 코드는 다시 읽지 않는다.
- 승인 단계에서는 직전 단계에서 승인된 내용을 다시 분석하거나 재작성하지 않고 그대로 실행한다.
- 코드 탐색과 리뷰는 `git diff`와 변경 파일부터 확인하고, 추가 문맥이 필요한 경우에만 주변 파일을 읽는다.
- 현재 Issue와 직접 관련 없는 프로젝트 파일은 탐색하지 않는다.
- 테스트 성공 로그는 전체를 반복해서 분석하지 않고 실행 결과와 실패 원인 중심으로 확인한다.
- 이미 보고한 내용은 다음 단계에서 반복해서 길게 설명하지 않는다.
- 각 단계에서는 해당 단계의 목적에 필요한 작업만 수행하고 다음 단계의 작업을 미리 수행하지 않는다.

## Global Codex Guidelines

### 1. Clarify Before Changing
- 요구사항이 불명확하면 임의로 결정하지 않는다.

### 2. Keep Changes Minimal
- 요청 범위에 필요한 최소한의 코드만 작성한다.
- 관련 없는 리팩터링, 추상화, 기능 추가를 하지 않는다.
- 기존 프로젝트 스타일을 따른다.

### 3. Verify Changes
- 변경된 동작을 적절한 테스트로 검증한다.
- 기존 실패나 명세 불일치를 테스트 수정이나 비활성화로 숨기지 않는다.

### 4. Report Concisely
- 새로 변경된 내용, 검증 결과, 남은 문제만 간단히 보고한다.
