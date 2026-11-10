# Current Task

## 작업과 승인 근거

- 2026-11-10 사용자 요청: 공통 요구사항·API·ERD의 공식 원본을 `marcel615/Ask-Teacher-specs`의 `main` 브랜치로 통일한다. Backend AI-DLC의 AGENTS.md, Skill, README와 관련 문서의 참조를 전환·검증한 뒤 Backend의 중복 세 문서를 삭제하고 PR을 생성한다. 기존 개발 워크플로우를 유지하며 merge는 사용자가 직접 한다.
- 사용자는 이번 문서 정비를 Issue 없이 진행하도록 승인했다. Issue 생성·선정·Project 상태 변경은 이번 작업에서 생략하며, 기존 Issue 기반 개발 절차 자체는 변경하지 않는다.
- 사용자는 기존 `develop`의 `src/main/resources/application.yaml` 미커밋 변경을 보존하고 별도 worktree에서 진행하도록 승인했다. 작업 위치는 `.worktrees/specs-source`, 브랜치는 이번 작업의 예외인 `feature/specs-source`이며 PR 방향은 `develop`이다. 원 작업 트리의 설정 파일은 수정하지 않는다.
- 이전 Issue #58의 PR #72 merge와 `docs/devlog/2026-11.md` 반영을 확인했다.
- 이후 사용자 지시로 이번 작업의 독립 리뷰·devlog 작성·CI 확인을 생략한다. 이 예외는 이번 문서 정비에만 적용하며 기존 개발 워크플로우는 유지한다.

## 공식 명세와 확인 근거

- [요구사항](https://github.com/marcel615/Ask-Teacher-specs/blob/main/docs/requirements.md)
- [API 명세](https://github.com/marcel615/Ask-Teacher-specs/blob/main/docs/api-spec.md)
- [ERD](https://github.com/marcel615/Ask-Teacher-specs/blob/main/docs/erd.md)
- 확인한 Specs `main` commit: `79fadfa8c687681186f0c0e1ef0e6e5e1dded33c`.
- 세 문서의 존재·접근을 확인했고, Backend 사본과 CRLF 정규화 후 내용이 모두 동일함을 확인했다. Blob SHA는 요구사항 `c494d4e370b1f70033816e009347da55411701f3`, API `1c8b0fff56cf6711c41e200052e83e99ac585ee6`, ERD `12750128722f9b23eb28ad9d768df936fa27a8ba`다.

## 목표와 범위

- `AGENTS.md`, 세 Skill, `README.md`, `.codex/agents/architect.toml` 및 관련 지침·템플릿·리뷰 문서의 명세 참조를 Specs `main`으로 통일한다.
- Backend에는 공식 명세를 복사하지 않고 작업별 요약과 공식 원본 링크를 둔다. README의 API 표는 개요이며 상세 계약은 Specs에서 확인하도록 안내한다.
- Architect의 삭제될 로컬 명세 수정 안내를 정리한다. `docs/current-task.md` 설계 작성과 독립 리뷰 역할은 유지하며 Specs 변경 필요성은 판단·보고한다. 이번 작업으로 Specs 쓰기 권한을 추가하지 않는다.
- 참조 전환과 검증을 마친 뒤 Backend의 `docs/requirements.md`, `docs/api-spec.md`, `docs/erd.md`를 삭제한다.
- 역사 devlog의 기존 기록과 경로 표현은 보존하고 이번 전환 기록을 append한다. 현재 설계·작업 기록에는 이번 승인 범위와 실제 검증 결과를 남긴다.
- Issue 선정, 역할별 순차 진행, 품질 게이트, 테스트, 리뷰, 기록, PR 및 사용자 직접 merge 절차는 유지한다.

## 제외 범위

- 요구사항·API 계약·ERD 내용 변경 및 Specs 저장소 수정.
- 제품 코드·테스트 동작, DB 구조·인증·인가, 비밀값·API 키·실제 환경 설정 변경.
- 외부 Issue·Project 수정, 기능 후보 등록 및 이번 작업과 무관한 정리.
- 역사 devlog 재작성과 사용자 merge 대행.

## 설계 게이트

- Main은 공식 원본 참조 전환과 지정한 중복 문서 삭제에 한정한 설계를 승인했다.
- 명세 내용 변경은 필요하지 않다. 세 문서는 확인한 Specs 원본과 내용이 동일하며 API·ERD·요구사항의 동작 변경은 이번 범위에 없다.
- 추가 추상화·설정·제품 테스트는 필요하지 않다. 참조의 정확성과 기존 개발 절차 보존을 중심으로 검증한다.

## 검증 계획과 완료 조건

- 숨김 디렉터리를 포함한 전체 추적 파일에서 로컬 명세 참조를 검색하고, 역사 기록·삭제 설명을 제외한 현재 지침과 링크가 공식 원본을 가리키는지 확인한다.
- 공식 명세 세 경로의 접근 및 삭제 전 내용 동일성 확인 근거를 기록한다. 새 Markdown 링크와 TOML·YAML 구문을 검증한다.
- `git diff --check`와 기본 검증 `.\gradlew.bat test`를 실행하고 실제 결과 및 미검증 사항을 기록한다.
- 이번 작업은 사용자 지시로 독립 리뷰와 devlog 작성을 생략한다.
- `feature/specs-source`에서 `develop`으로 PR을 생성한다. 사용자 지시로 CI 확인을 생략하며 미확인 상태를 성공으로 보고하지 않는다. Merge는 사용자가 직접 한다.
