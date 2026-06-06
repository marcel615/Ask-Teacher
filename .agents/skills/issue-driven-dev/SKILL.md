---
name: issue-driven-dev
description: GitHub Issue 기반 개발 워크플로우가 필요할 때 사용한다. Issue를 current-task.md로 정리하고, develop에서 feature 브랜치를 생성해 구현/테스트/PR/리뷰/devlog까지 진행하는 작업에 사용한다.
---

# Issue-driven Development Skill

## 역할

- Architect: Issue 분석, current-task.md 초안, PR 리뷰, devlog
- Builder: 승인된 구현, 테스트, PR 준비
- Reporter: README / 포트폴리오 기록 초안

## 핵심 원칙

- Issue는 원격 GitHub의 작업 요청 원본이다.
- 구현 기준은 `docs/current-task.md`다.
- API / ERD / 요구사항 전체는 각각 `docs/api-spec.md`, `docs/erd.md`, `docs/requirements.md`에 둔다.
- Issue 분석 시 `current-task.md`만 작성하지 않고, `requirements.md` / `api-spec.md` / `erd.md` 변경 필요 여부를 먼저 판단한다.
- `current-task.md`에는 이번 Issue 관련 API/ERD 요약과 참조만 둔다.
- 구현은 `develop`에서 생성한 `feature/*` 브랜치에서 진행한다.
- PR 방향은 `feature/*` → `develop`이다.
- 리뷰 기준은 `docs/review-checklist.md`에 둔다.
- devlog는 `docs/devlog/YYYY-MM.md`에 월별로 누적한다.
- 승인 전 파일 수정, 브랜치 변경, git add/commit/push, PR 생성을 하지 않는다.

## 흐름

1. Issue 조회

```bash
gh issue view ISSUE_NUMBER --json title,body,labels,state,url
```

2. Architect가 Issue를 분석한다.
- docs/requirements.md 변경 필요 여부를 판단한다.
- docs/api-spec.md 변경 필요 여부를 판단한다.
- docs/erd.md 변경 필요 여부를 판단한다.
- 설계 문서 변경이 필요하면 각 문서별 수정안 초안을 먼저 작성한다.
- 파일은 바로 수정하지 않고 사용자 승인 후 반영한다.

3. Architect가 `docs/current-task.md` 초안을 작성한다.
- current-task.md에는 이번 Issue와 직접 관련된 API/ERD/요구사항 요약과 참조만 작성한다.

4. 사용자 승인 후 `current-task.md`에 반영한다.
5. Builder가 구현 계획과 브랜치 계획을 제안한다.
6. 사용자 승인 후 develop을 최신화하고 feature 브랜치를 만든다.

```bash
git status
git fetch origin
git checkout develop
git pull origin develop
git checkout -b feature/issue-번호-작업명
```

7. Builder는 `current-task.md` 범위 안에서만 구현한다.
8. 필요한 경우 `docs/api-spec.md`, `docs/erd.md`, `docs/requirements.md`를 필요한 부분만 참고한다.
9. 구현 후 테스트를 실행한다.

```bash
./gradlew test
```

10. 변경 파일, 테스트 결과, git diff, PR 초안을 요약한다.
11. 사용자 승인 후 `feature/*` → `develop` PR을 만든다.
12. Architect가 `docs/review-checklist.md` 기준으로 리뷰한다.
13. Architect가 `docs/devlog.md` 초안을 작성한다.
12. Reporter 또는 Architect가 `docs/devlog/YYYY-MM.md`에 추가할 기록 초안을 작성한다.

## 중단 조건

아래 상황이면 작업을 멈추고 사용자에게 보고한다.

- `git status`가 clean하지 않음
- pull 중 충돌 발생
- current-task.md 범위를 벗어난 변경 필요
- DB 구조 또는 인증/인가 구조 변경 필요
- 삭제, reset, clean 등 위험 명령 필요
