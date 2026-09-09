# Quick Prompts

## 0-0. 에이전트 시작

Architect:

```text
너는 Architect Codex야.
AGENTS.md를 먼저 읽고 기준으로 작업해줘.

시작 단계에서는 다른 프로젝트 파일을 읽지 마.
Issue나 리뷰 대상이 주어지면 그때 필요한 문서/코드만 좁혀서 읽어.

Github는 참조 가능하고, 설계/리뷰 중심으로 진행해.
파일 수정과 명령어 실행은 승인된 범위에서만 해.
```

Builder:

```text
너는 Builder Codex야.
AGENTS.md를 먼저 읽고 기준으로 작업해줘.

시작 단계에서는 다른 프로젝트 파일을 읽지 마.
current-task.md 승인 후 필요한 문서/코드만 좁혀서 읽어.

구현/테스트 중심으로 진행해.
구현 중 트러블슈팅, 주요 의사결정, 예상과 다른 동작,
중요한 테스트/검증 결과가 발생하면 `docs/current-work-log.md`에 간단히 기록해.

승인 전 파일 수정, 브랜치 변경, git 명령은 하지 마.
```

Reporter:

```text
너는 Reporter Codex야.
AGENTS.md를 먼저 읽고 기준으로 작업해줘.

시작 단계에서는 다른 프로젝트 파일을 읽지 마.
Issue/PR/devlog 대상이 주어지면 그때 필요한 문서만 좁혀서 읽어.

기록/요약 중심으로 진행해.
승인 전 파일 수정은 하지 마.
```

## 0-1. Issue 확장 준비

```text
Issue #번호 확장해서 작성하자.

현재 Issue와 `.github/ISSUE_TEMPLATE/feature_request.yml`을 확인해줘.

바로 Issue 초안을 완성하지 말고,
현재 내용에서 부족하거나 결정이 필요한 부분을 먼저 찾아서 나에게 질문해줘.

질문은 이번 Issue의 목표, 작업 범위, 완료 조건, 제외 범위를 명확히 하는 데 필요한 것 등으로 해줘.

내 답변을 받은 뒤 기존 Issue의 의도를 유지하면서
템플릿 형식에 맞는 Issue 수정안을 작성해줘.

아직 GitHub Issue는 수정하지 마.
```

## 0-2. Issue 수정 승인

```text
Issue 수정안 승인.

최종 제목과 본문을 보여준 뒤
현재 GitHub Issue에 반영해줘.
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

새 Issue 작업을 시작하는 경우 `docs/current-work-log.md`에 이전 작업 기록이 남아 있다면,
구현 시작 시 현재 Issue 기준으로 초기화할 계획도 함께 제안해줘.

아직 파일 수정, 브랜치 이동, 브랜치 생성은 하지 마.
```

## 4. 구현 승인

```text
브랜치 생성과 구현 계획 승인.

develop을 최신화하고 제안한 feature 브랜치에서 구현해줘.
새 Issue 작업이라면 `docs/current-work-log.md`를 현재 Issue 기준으로 초기화해줘.

승인된 Architect 사전 반영 문서 변경은 되돌리지 말고, 생성한 feature 브랜치에서 그대로 유지한 상태로 구현을 진행해줘.
구현 중 발생한 트러블슈팅, 주요 의사결정, 예상과 다른 동작,
중요한 테스트/검증 결과는 `docs/current-work-log.md`에 간단히 기록해.

구현 후 ./gradlew test 실행,
새 API 또는 변경된 API가 있다면 .http 파일 추가/수정 및 가능한 범위에서 수동 확인,
변경 파일, 테스트 결과, 수동 API 확인 결과, git diff 요약, PR 초안을 보여줘.

아직 git add, commit, push, PR 생성은 하지 마.
```

## 5. PR 준비

```text
PR 준비하자.

feature/* → develop 방향으로 PR 본문과 커밋 메시지를 제안해줘.
PR 본문은 `.github/PULL_REQUEST_TEMPLATE/pull_request_template.md` 템플릿 형식을 기준으로 작성해줘.
테스트 결과와 체크리스트는 실제로 확인한 항목만 체크해줘.
커밋 메시지는 Conventional Commit 형식으로 제안해줘.
아직 git add, commit, push, gh pr create는 하지 마.
```

## 6. PR 생성 승인

```text
PR 본문과 커밋 메시지 승인.
최종 명령어 목록을 보여준 뒤,
feature 브랜치 push와 develop 대상 PR 생성을 진행해줘.
```

## 7. PR 리뷰 준비

```text
현재 PR을 리뷰하자.
docs/review-checklist.md 기준으로 현재 PR을 리뷰해줘.
PR에는 아직 리뷰를 남기지 말고, 작성할 리뷰 내용을 먼저 나에게 보여줘.
리뷰는 반드시 수정 / 선택 개선 / 통과 항목으로 나눠줘.
```

## 8. PR 리뷰 등록 승인

```text
PR 리뷰 내용 승인.
방금 작성한 리뷰 내용을 현재 PR에 리뷰로 등록해줘. 
리뷰 본문에는 Architect가 작성한 리뷰임을 명확히 표시해줘.
```

## 9. 리뷰 반영 수정 준비

```text
PR에 등록된 Architect 및 사용자 리뷰를 검토해줘.
각 리뷰 항목을 필수 수정 / 선택 개선 / 보류 가능으로 분류하고,
대상 파일과 수정 방향을 제안해줘.
아직 파일은 수정하지 마.
```

## 10. 리뷰 반영 수정 승인

```text
리뷰 반영 수정안 승인.
방금 제안한 수정안 중 승인한 항목만 파일에 반영해줘.

리뷰 반영 과정에서 새로 발생한 트러블슈팅, 주요 의사결정,
예상과 다른 동작, 중요한 테스트/검증 결과가 있다면
`docs/current-work-log.md`에 추가로 기록해.

수정 완료 후 변경된 파일과 반영 내용을 요약해줘.

아직 git add, commit, push는 하지 마.
```

## 11. 리뷰 반영 커밋 및 Push

```text
리뷰 반영 수정 내용을 커밋하고 push하자.
변경사항을 확인한 뒤 Conventional Commit 형식의 커밋 메시지를 제안해줘.
최종 명령어 목록을 보여준 뒤,
리뷰 반영 수정사항을 commit하고 현재 feature 브랜치에 push해줘.
기존 PR에 정상 반영되었는지도 확인해줘.
```

## 12. devlog

```text
이번 Issue와 PR 기준으로 docs/devlog/YYYY-MM.md에 추가할 기록 초안을 작성해줘.
Issue, PR과 함께 `docs/current-work-log.md`가 존재하면 참고해서
구현 중 발생한 트러블슈팅, 주요 의사결정, 테스트/검증 결과가 누락되지 않도록 해줘.

규칙:
- 기존 devlog 전체를 재작성하지 마.
- 기존 기록은 수정하지 마.
- 현재 Issue/PR에 대한 기록만 새 항목으로 추가해.
- 형식이 필요하면 가장 최근 항목 하나만 참고해.
- 파일 맨 아래에 append-only로 추가하는 기준으로 작성해.

devlog에는 아래 항목을 포함해줘.

- Issue / PR
- 오늘의 작업
- 변경 내용
- 테스트 결과 / 수동 확인 결과
- 문제 / 해결
- AI 활용 기록
- 다음 작업 후보

아직 파일은 수정하지 마.
```

## 13. 작업 중단 / 확인 요청

```text
현재 작업을 잠시 멈추고 상태를 정리해줘.

아래 항목을 기준으로 보고해줘.

- 현재 브랜치
- git status
- 지금까지 변경한 파일
- 완료한 작업
- 남은 작업
- 현재 `docs/current-work-log.md`에 기록된 주요 내용
- 확인이 필요한 문제
- 다음에 실행할 명령 또는 작업 제안

파일 수정이나 git 명령은 추가로 실행하지 마.
```