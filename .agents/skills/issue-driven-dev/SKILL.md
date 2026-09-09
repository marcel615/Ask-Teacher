---
name: issue-driven-dev
description: GitHub Issue 기반 개발 워크플로우가 필요할 때 사용한다. Issue를 current-task.md로 정리하고, develop에서 feature 브랜치를 생성해 구현/테스트/PR/리뷰/devlog까지 진행하는 작업에 사용한다.
---

# Issue-driven Development Skill

## 역할

- Architect: Issue 작성/확장 협의, Issue 분석, 설계 문서/current-task.md 초안, PR 리뷰
- Builder: 승인된 구현, 테스트, PR 준비, 구현 작업 기록
- Reporter: devlog / README / 포트폴리오 기록 초안

## 핵심 원칙

- Issue 작성/확장 시 `.github/ISSUE_TEMPLATE`의 해당 템플릿을 기준으로 한다.
- Issue 내용이 부족하면 Architect가 임의로 요구사항을 확정하지 않고, 목표/범위/완료 조건/제외 범위 등 필요한 사항을 사용자와 먼저 협의한다.
- GitHub Issue 생성/수정은 수정안을 먼저 제시하고 사용자 승인 후 진행한다.
- Issue는 원격 GitHub의 작업 요청 원본이다.
- 구현 기준은 `docs/current-task.md`다.
- API / ERD / 요구사항 전체는 각각 `docs/api-spec.md`, `docs/erd.md`, `docs/requirements.md`에 둔다.
- Issue 분석 시 `current-task.md`만 작성하지 않고, `requirements.md` / `api-spec.md` / `erd.md` 변경 필요 여부를 먼저 판단한다.
- `current-task.md`에는 이번 Issue 관련 API/ERD 요약과 참조만 둔다.
- 구현은 `develop`에서 생성한 `feature/*` 브랜치에서 진행한다.
- 구현 중 작업 기록은 `docs/current-work-log.md`에 임시로 관리한다.
- `docs/current-work-log.md`는 새 Issue 구현 시작 시 현재 Issue 기준으로 초기화한다.
- Builder는 트러블슈팅, 주요 의사결정, 예상과 다른 동작, 중요한 테스트/검증 결과만 작업 기록에 남긴다.
- PR 방향은 `feature/*` → `develop`이다.
- 리뷰 기준은 `docs/review-checklist.md`에 둔다.
- devlog는 `docs/devlog/YYYY-MM.md`에 월별로 누적한다.
- Reporter는 devlog 작성 시 Issue, PR, `docs/current-work-log.md`를 함께 참고한다.
- 승인 전 파일 수정, 브랜치 변경, git add/commit/push, PR 생성을 하지 않는다.

## 흐름

1. Issue를 조회하고 해당 Issue 템플릿을 확인한다.

```bash
gh issue view ISSUE_NUMBER --json title,body,labels,state,url
```

2. 필요한 경우 Architect가 Issue 확장 협의를 진행한다.
- 현재 Issue에서 부족하거나 결정이 필요한 내용을 찾는다.
- 사용자에게 필요한 질문만 제시한다.
- 사용자 답변을 바탕으로 Issue 수정안을 작성한다.
- 바로 GitHub Issue를 수정하지 않는다.
- 사용자 승인 후 기존 Issue에 반영한다.

3. Architect가 Issue를 분석한다.
- docs/requirements.md 변경 필요 여부를 판단한다.
- docs/api-spec.md 변경 필요 여부를 판단한다.
- docs/erd.md 변경 필요 여부를 판단한다.
- 설계 문서 변경이 필요하면 각 문서별 수정안 초안을 먼저 작성한다.
- 파일은 바로 수정하지 않고 사용자 승인 후 반영한다.

4. Architect가 `docs/current-task.md` 초안을 작성한다.
- current-task.md에는 이번 Issue와 직접 관련된 API/ERD/요구사항 요약과 참조만 작성한다.

5. 사용자 승인 후 `current-task.md`에 반영한다.
6. Builder가 구현 계획과 브랜치 계획을 제안한다.
7. 사용자 승인 후 develop을 최신화하고 feature 브랜치를 만든다.

```bash
git status
git fetch origin
git checkout develop
git pull origin develop
git checkout -b feature/issue-번호-작업명
```

8. Builder는 `current-task.md` 범위 안에서만 구현한다.
9. 구현 중 트러블슈팅, 주요 의사결정, 예상과 다른 동작, 중요한 테스트/검증 결과가 발생하면 `docs/current-work-log.md`에 간단히 기록한다.
10. 필요한 경우 `docs/api-spec.md`, `docs/erd.md`, `docs/requirements.md`를 필요한 부분만 참고한다.
11. 구현 후 테스트를 실행한다.

```bash
./gradlew test
```

12. 변경 파일, 테스트 결과, git diff, PR 초안을 요약한다.
13. 사용자 승인 후 `feature/*` → `develop` PR을 만든다.
14. Architect가 `docs/review-checklist.md` 기준으로 리뷰 초안을 작성한다.
15. 사용자 승인 후 Architect 리뷰를 PR에 등록한다.
16. Builder가 PR 리뷰를 검토하고 반영안을 제안한다.
17. 사용자 승인 후 승인된 리뷰 항목만 수정한다.
18. 리뷰 반영 중 새로 발생한 트러블슈팅이나 주요 의사결정도 docs/current-work-log.md에 추가한다.
19. 리뷰 반영 내용을 commit/push하여 기존 PR에 반영한다.
20. Reporter가 Issue, PR, docs/current-work-log.md를 참고하여 docs/devlog/YYYY-MM.md에 추가할 기록 초안을 작성한다.

## 중단 조건

아래 상황이면 작업을 멈추고 사용자에게 보고한다.

- `git status`에 이번 Issue의 승인된 변경으로 설명되지 않는 수정사항이 존재함
- pull 중 충돌 발생
- current-task.md 범위를 벗어난 변경 필요
- DB 구조 또는 인증/인가 구조 변경 필요
- 삭제, reset, clean 등 위험 명령 필요
