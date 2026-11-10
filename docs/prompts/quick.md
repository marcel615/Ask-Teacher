# Quick Prompts

상세 절차와 승인 경계는 `.agents/skills/issue-driven-dev/SKILL.md`와 `AGENTS.md`를 따른다.
아래 문구는 예시이며 같은 뜻의 자연스러운 지시도 사용할 수 있다.
공통 명세는 `ai-dlc.yaml`의 활성 서비스 `requirements`, `contracts.api`, `contracts.erd`가 지정한 공식 원본을 확인한다.
Main은 품질 게이트를 확인하고 승인된 Issue 범위에서 진행한다.
사용자 판단이 필요한 상황과 PR merge는 사용자에게 남긴다.

## Issue와 설계

- 시작: `Issue #번호 작업 시작해.`
- 다음 Issue 선정: `작업 시작해. 다음 Issue를 확인해줘.`
- 기능 후보: `이 아이디어를 GitHub Projects의 Pending 후보로 추가해줘.`
- 기능 탐색: `기능 아이디어 제안해.`
- 백로그 정제: `승인한 기능 후보의 백로그를 정제해.` (조건을 충족한 Approved Draft는 별도 전환 승인 없이 최대 2개를 정식 Issue로 전환한다.)
- Issue 초안 승인: `Issue 초안을 승인하고 GitHub에 등록해.`
- Issue 수정 승인: `Issue 수정안을 승인하고 반영해.`
- 상태 확인: `현재 작업 상태와 품질 게이트 결과를 알려줘.`
- 예외 확인: `멈춘 이유와 내가 결정해야 할 사항을 알려줘.`

## 종료와 중단

- 정리: `PR 머지했어. 확인 후 기준 브랜치 최신화와 브랜치 정리를 진행해.`
- 중단: `작업을 멈추고 현재 상태와 남은 작업을 알려줘.`
