# AGENTS.md

## 핵심 규칙

- 사용자는 PM이자 최종 승인자다.
- 한 번에 하나의 Issue만 진행한다.
- 구현 기준은 `docs/current-task.md`다.
- `docs/current-task.md` 범위 밖은 구현하지 않는다.
- 파일 수정, 브랜치 생성/이동, git add/commit/push, PR 생성, 삭제, DB/인증/인가 변경은 사용자 승인 후 진행한다.

## 브랜치

- 기준 브랜치: `develop`
- 작업 브랜치: `feature/issue-번호-작업명`
- PR 방향: `feature/*` → `develop`
- `main`이나 `develop`에서 직접 구현하지 않는다.

## 참고 문서

- 상세 워크플로우: `.agents/skills/issue-driven-dev/SKILL.md`
- 빠른 지시문: `prompts/quick.md`
- 요구사항: `docs/requirements.md`
- API 명세: `docs/api-spec.md`
- ERD: `docs/erd.md`
- 리뷰 기준: `docs/review-checklist.md`
- 작업 기록: `docs/devlog/YYYY-MM.md`
- PR 템플릿: `.github/PULL_REQUEST_TEMPLATE/pull_request_template.md`

## 테스트

```bash
./gradlew test
```

## 수동 API 확인

- 프론트엔드가 없는 동안 `.http` 파일을 수동 API 확인용으로 사용한다.
- `.http` 파일은 자동 테스트를 대체하지 않으며, 기본 검증은 `./gradlew test`다.
- 새 API 또는 변경된 API가 있으면 관련 `.http` 파일을 추가/수정하고 가능한 범위에서 실행 확인한다.
- `.http` 파일 위치는 기존 구조를 유지한다: `src/main/java/com/github/marcel615/askteacher/http`
- 수동 API 확인 결과는 PR 요약 또는 devlog에 기록한다.

## Global Codex Guidelines

These guidelines reduce common LLM coding mistakes. Prefer caution over speed.

### 1. Think Before Coding

Before implementing:
- State assumptions explicitly.
- If requirements are unclear, ask or clearly name the uncertainty.
- If multiple interpretations exist, present them instead of silently choosing one.
- If a simpler approach exists, prefer it.
- Push back when the requested solution seems overcomplicated or risky.

### 2. Simplicity First

Implement the minimum code that solves the task.

- Do not add features beyond the request.
- Do not create abstractions for single-use code.
- Do not add configurability or flexibility that was not requested.
- Do not over-engineer error handling for unrealistic scenarios.
- If the solution becomes much larger than necessary, simplify it.

### 3. Surgical Changes

Touch only what is required.

- Do not refactor unrelated code.
- Do not improve adjacent formatting, comments, or structure unless required.
- Match the existing project style.
- If unrelated dead code or problems are found, mention them instead of changing them.
- Remove only unused imports, variables, or functions introduced by your own changes.

Every changed line should directly support the requested task.

### 4. Goal-Driven Execution

Turn the task into verifiable goals.

For multi-step tasks:
1. State a brief plan.
2. Define success criteria.
3. Implement the smallest necessary change.
4. Verify with tests, build commands, or manual checks.
5. Report changed files and verification results.

Examples:
- "Add validation" → test invalid inputs and make them fail correctly.
- "Fix a bug" → reproduce the bug, fix it, then verify the fix.
- "Refactor" → ensure behavior is unchanged before and after.

### 5. Final Response

After implementation, summarize:
- What changed
- Files changed
- How it was verified
- Remaining TODOs or assumptions
