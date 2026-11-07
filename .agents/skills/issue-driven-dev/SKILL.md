---
name: issue-driven-dev
description: GitHub Issue를 분석하고 사용자 승인 아래 설계, 구현, 테스트, PR, 리뷰와 devlog를 진행한다.
---

# Issue-driven Development

공통 규칙과 승인 범위는 `AGENTS.md`를 따른다. Main은 한 번에 한 Issue를 조율하고 Architect → Builder → Architect 리뷰 → Reporter를 순차 호출한다. Subagent를 호출할 수 없거나 필요한 결과를 받지 못하면 중단하고 보고한다. Main은 이 스킬을 읽고 각 인계에 Issue, 해당 단계의 작업·필수 규칙, 사용자 승인 범위와 미승인 행동, 관련 파일, 직전 결과와 기대 산출물을 전달한다. Subagent는 스킬 전체를 매번 읽을 필요가 없으며 필요한 문서만 확인한다. 작업이나 승인 범위가 불명확하면 추측하지 않고 Main에게 확인한다. 같은 파일을 동시에 수정하지 않는다.

## 역할

- Main: 작업과 승인 범위 관리, 결과 확인, 사용자 보고.
- Architect: Issue 분석·설계, 승인된 설계 문서 반영과 독립적인 변경 리뷰.
- Builder: 승인된 구현·테스트·PR 준비와 필요한 임시 작업 기록.
- Reporter: Issue, PR, 임시 기록을 참고한 devlog 초안.

## 진행 순서

1. Main은 현재 Issue, 단계, 승인 내역과 Git 상태를 확인한다. 같은 Issue를 재개할 때는 현재 기록과 상태를 보존하고 이어간다. 새 Issue로 전환할 때는 이전 Issue의 종료와 devlog 반영 여부를 확인한다. 승인되지 않은 변경이나 필요한 확인이 불가능하면 중단한다.
2. Architect는 Issue와 템플릿을 확인한다. 부족한 요구사항은 사용자에게 확인하고 Issue 변경이 필요하면 초안을 제안한다. Issue 생성·수정은 사용자 승인 후 진행한다.
3. Architect는 요구사항·API·ERD의 변경 필요 여부를 판단하고 `docs/current-task.md` 및 필요한 설계 문서 초안을 제안한다. Architect는 사용자 승인 후 승인된 current-task.md, requirements.md, api-spec.md, erd.md 변경만 반영한다. 리뷰는 읽기 전용으로 수행한다. 전체 명세는 복사하지 않고 Issue 관련 요약과 참조를 둔다.
4. Builder는 승인된 범위의 구현·브랜치 계획을 제안한다. 사용자 승인 후 develop 최신 상태와 브랜치 전환 가능 여부를 확인하고 feature 브랜치를 만든다. 승인된 사전 문서 변경을 보존하며 충돌 또는 불확실성이 있으면 중단한다.
5. 새 Issue를 시작할 때는 이전 Issue의 devlog 반영을 확인한 후 승인 범위에서 `docs/current-work-log.md`를 초기화한다. 같은 Issue를 재개할 때는 초기화하지 않는다. Builder는 주요 결정, 문제·해결, 예상과 다른 동작, 중요한 검증 결과만 기록한다.
6. Builder는 승인된 범위에서 구현하고 관련 자동 테스트와 전체 테스트를 실행한다. 변경 파일, 결과, 미검증 사항과 PR 초안을 Main에 전달한다.
7. Main은 사용자 승인 후 승인된 commit·push와 PR 생성을 진행한다.
8. Architect는 변경 diff와 `docs/review-checklist.md`를 기준으로 필수 수정·선택 개선·통과 항목과 근거를 반환한다. PR 리뷰 등록은 사용자 승인 후 진행한다.
9. Builder는 리뷰 반영안을 제안하고 승인된 항목만 수정·재검증한다. 리뷰 수정 중 발생한 주요 결정, 문제·해결과 중요한 검증 결과도 승인 범위에서 임시 작업 기록에 추가한다. 리뷰와 수정은 최대 2회 반복하고 필수 문제가 남거나 범위 변경이 필요하면 중단해 보고한다. 후속 commit·push도 사용자 승인 범위에 따른다.
10. Reporter는 Issue, PR, 임시 기록을 바탕으로 월별 devlog의 새 항목만 초안으로 반환한다. 사용자 승인 후 append-only로 반영하고 승인된 commit·push를 수행한다.
11. 사용자가 PR을 merge한다. Main은 merge와 devlog 반영을 확인하고 승인된 develop 이동·최신화를 수행한다. 이번 Issue의 feature 브랜치는 merge와 미반영 커밋·변경이 없음을 확인한 경우 별도 승인 없이 정리할 수 있다. 실행 환경의 권한 정책은 별도로 따른다.
12. Main은 테스트·CI의 확인된 결과, 미검증 사항, 브랜치·PR 상태와 남은 작업을 보고하고 종료한다.

## 중단 조건

- 승인되지 않은 변경, 브랜치 전환 위험 또는 pull 충돌
- 승인 범위 밖 변경이나 DB·인증·인가 변경 필요
- Subagent 호출 실패, 필수 리뷰 문제 미해결 또는 검증 실패
- 새 Issue로 전환할 때 이전 devlog 반영 확인 불가, 또는 브랜치 정리 시 PR merge나 미반영 내용 확인 불가
- 위에서 허용한 feature 브랜치 정리를 제외한 삭제, reset, clean 등 승인되지 않은 위험 작업
