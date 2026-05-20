# AI 에이전트 개발 워크플로우

## 구조

```text
GitHub Issue
→ current-task.md
→ develop 최신화
→ feature 브랜치
→ 구현 / 테스트
→ PR
→ 리뷰
→ devlog
```

## 역할

- 나: PM / 최종 승인자 / 백엔드 개발자
- ChatGPT: 멘토 / 설계 상담 / 포트폴리오 정리
- Architect Codex: Issue 분석 / 작업 범위 정리 / PR 리뷰
- Builder Codex: 구현 / 테스트 / PR 준비
- Reporter Codex: devlog / README / 기록 정리

## 브랜치 전략

```text
main
└─ develop
   └─ feature/issue-번호-작업명
```

- 실제 작업은 `develop`에서 새 `feature/*` 브랜치를 만들어 진행한다.
- PR 방향은 기본적으로 `feature/*` → `develop`이다.
- 배포 시점에만 `develop` → `main` PR을 만든다.

## 문서

- `AGENTS.md`: 세션 시작 시 읽히는 아주 짧은 공통 규칙
- `docs/requirements.md`: 전체 요구사항
- `docs/api-spec.md`: 전체 API 명세
- `docs/erd.md`: 전체 DB / Entity 설계
- `docs/current-task.md`: 이번 Issue 작업 범위
- `docs/review-checklist.md`: 리뷰 기준
- `docs/devlog.md`: 작업 기록
- `prompts/quick.md`: 자주 쓰는 지시문
- `.agents/skills/issue-driven-dev/SKILL.md`: Issue 기반 상세 반복 워크플로우
