# Quick Prompts

## 0. 에이전트 시작

Architect:

```text
너는 Architect Codex야.
AGENTS.md와 prompts/quick.md만 먼저 읽고 기준으로 작업해줘.

시작 단계에서는 다른 프로젝트 파일을 읽지 마.
Issue나 리뷰 대상이 주어지면 그때 필요한 문서/코드만 좁혀서 읽어.

Github는 참조 가능하고, 설계/리뷰 중심으로 진행해.
파일 수정과 명령어 실행은 승인된 범위에서만 해.
```

Builder:

```text
너는 Builder Codex야.
AGENTS.md와 prompts/quick.md만 먼저 읽고 기준으로 작업해줘.

시작 단계에서는 다른 프로젝트 파일을 읽지 마.
current-task.md 승인 후 필요한 문서/코드만 좁혀서 읽어.

구현/테스트 중심으로 진행해.
승인 전 파일 수정, 브랜치 변경, git 명령은 하지 마.
```

Reporter:

```text
너는 Reporter Codex야.
AGENTS.md와 prompts/quick.md만 먼저 읽고 기준으로 작업해줘.

시작 단계에서는 다른 프로젝트 파일을 읽지 마.
Issue/PR/devlog 대상이 주어지면 그때 필요한 문서만 좁혀서 읽어.

기록/요약 중심으로 진행해.
승인 전 파일 수정은 하지 마.
```

## 1. Issue → current-task

```text
Issue #번호 작업하자.

gh issue view 번호 --json title,body,labels,state,url 로 조회하고,
아래 순서로 초안을 작성해줘.

1. 이 Issue가 requirements.md / api-spec.md / erd.md 변경을 필요로 하는지 판단
2. 변경이 필요하다면 각 문서별 수정안 초안 작성
3. docs/current-task.md 초안 작성

current-task.md 초안에는 다음도 포함해줘.

- Architect가 승인받아 수정한 문서를 `예상 변경 파일 > Architect 사전 반영 문서`에 기록
- `docs/current-task.md`는 current-task 확정 시 항상 포함
- `docs/requirements.md`, `docs/api-spec.md`, `docs/erd.md`는 실제 변경이 필요한 경우에만 포함
- ERD 변경이 필요하지 않다면 `ERD 변경 없음`과 사유를 기록
- Builder가 구현 중 생성/수정할 수 있는 파일은 `예상 변경 파일 > Builder 구현 변경 예상 파일`에 따로 기록

아직 파일은 수정하지 마.
내가 승인하면 필요한 문서와 current-task.md에 반영해.
```

## 2. current-task 승인

```text
current-task.md 초안 승인.
docs/current-task.md에 반영해줘.
```

## 3. 구현 계획 + 브랜치 제안

```text
docs/current-task.md 기준으로 구현 계획 세우자.
먼저 git status, git branch --show-current, git remote -v만 확인해.
develop 최신화와 feature 브랜치 생성 계획을 제안해줘.
git status에서 변경된 문서가 보이면 docs/current-task.md의 `예상 변경 파일 > Architect 사전 반영 문서`에 포함된 파일인지 확인하고,
포함되어 있다면 이번 Issue의 승인된 사전 문서 변경으로 간주해 feature 브랜치에 함께 가져갈 계획을 세워줘.
아직 파일 수정, 브랜치 이동, 브랜치 생성은 하지 마.
```

## 4. 구현 승인

```text
브랜치 생성과 구현 계획 승인.
develop을 최신화하고 제안한 feature 브랜치에서 구현해줘.
승인된 Architect 사전 반영 문서 변경은 되돌리지 말고, 생성한 feature 브랜치에서 그대로 유지한 상태로 구현을 진행해줘.
구현 후 ./gradlew test 실행,
새 API 또는 변경된 API가 있다면 .http 파일 추가/수정 및 가능한 범위에서 수동 확인,
변경 파일, 테스트 결과, 수동 API 확인 결과, git diff 요약, PR 초안을 보여줘.
아직 git add, commit, push, PR 생성은 하지 마.
```

## 5. PR 준비

```text
PR 준비하자.
feature/* → develop 방향으로 PR 본문과 커밋 메시지를 제안해줘.
아직 git add, commit, push, gh pr create는 하지 마.
```

## 6. PR 생성 승인

```text
PR 본문과 커밋 메시지 승인.
최종 명령어 목록을 보여준 뒤,
feature 브랜치 push와 develop 대상 PR 생성을 진행해줘.
```

## 7. PR 리뷰

```text
docs/review-checklist.md 기준으로 현재 PR을 리뷰해줘.
파일은 수정하지 말고 반드시 수정 / 선택 개선 / 통과 항목으로 나눠줘.
```

## 8. 리뷰 반영 판단

```text
Architect 리뷰를 검토해줘.
각 항목을 필수 수정 / 선택 개선 / 보류 가능으로 분류하고,
아직 파일은 수정하지 마.
```

## 9. devlog

```text
이번 Issue와 PR 기준으로 docs/devlog.md에 추가할 기록 초안을 작성해줘.
아직 파일은 수정하지 마.
```
