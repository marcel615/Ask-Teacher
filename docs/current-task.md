# Current Task

## Issue

- Issue: #53
- Title: [Feature] README에 최신 AI-DLC 사용법 반영
- URL: https://github.com/marcel615/Ask-Teacher/issues/53
- 관련 사용자 결정 코멘트: 없음 (Main 확인)

## 목표

README에 현재 설정과 지침에 맞는 AI-DLC 사용법을 안내한다. 설정의 존재와
실행 검증 여부를 구분하고 확인되지 않은 기능을 확정적으로 기술하지 않는다.

## 범위

- Main, Architect, Builder, Reporter의 역할과 책임 설명
- 자연어 작업 시작 예시와 Issue 선정부터 설계 게이트, 구현·검증, PR,
  독립 리뷰, devlog, 최종 head 검증, 사용자 머지 및 정리까지의 흐름 설명
- 사용자 판단과 승인, 사용자 직접 머지 및 중단이 필요한 상황 안내
- 확인된 주요 지침 문서 및 역할 설정 경로 안내
- 현재 설정된 절차와 미구현·미검증 기능 구분
- 기존 프로젝트 소개와 실행 방법 유지

## 제외 범위

- Java 소스 및 테스트 코드, API, Entity, DB, 인증·인가 변경
- AI-DLC 워크플로우 자체 변경 및 새 Subagent·Skill 구현
- GitHub Projects 및 Jira 연동 구현
- Issue와 무관한 README 정비 또는 전체 재작성

## 설계 근거

- `AGENTS.md`와 `.agents/skills/issue-driven-dev/SKILL.md`가 역할,
  품질 게이트, 사용자 개입 지점과 중단 조건을 정의한다.
- `docs/prompts/quick.md`의 작업 시작 문구는 자연어 예시이며 고정 CLI
  명령이 아니다. 설계·구현 승인은 Main의 품질 게이트 판단이다.
- `.codex/agents/architect.toml`, `.codex/agents/builder.toml`,
  `.codex/agents/reporter.toml`이 존재한다. AI 리뷰는 필수 사람 리뷰를
  대신하지 않으며 PR merge는 사용자가 직접 수행한다.
- CI 설정의 존재만으로 이번 PR의 성공을 보장하지 않는다.

## 요구사항·API·ERD

- `docs/requirements.md`: 변경 없음. 사용자 기능을 변경하지 않는다.
- `docs/api-spec.md`: 변경 없음. API 동작을 변경하지 않는다.
- `docs/erd.md`: 변경 없음. Entity와 DB 구조를 변경하지 않는다.

## 검증 계획

- README의 각 주장과 문서 경로를 실제 설정 및 지침과 대조하고 링크 대상의
  존재를 확인한다.
- Markdown 문법과 렌더링, 기존 소개와 실행 방법의 보존을 확인한다.
- `git diff --check`와 기본 검증 `.\gradlew.bat test`를 실행한다.
- 테스트 범위, 결과와 미검증 사항을 작업 기록과 PR 또는 devlog에 기록한다.
- 최종 push의 정확한 head를 기준으로 CI와 필수 리뷰·보호 조건을 확인한다.

## 예상 변경 파일

- `docs/current-task.md`: 승인된 목표, 범위 및 검증 계획
- `README.md`: AI-DLC 사용 안내
- `docs/current-work-log.md`: 이전 Issue의 devlog 반영 확인 후 초기화하고 작업 기록
- `docs/devlog/2026-11.md`: Issue 작업과 검증 결과 기록
- 필요 시 승인된 후속 후보 기록

## 완료 조건

- [ ] README의 역할, 작업 흐름, 사용자 개입과 예외 안내가 실제 지침과 일치한다.
- [ ] 현재 설정과 향후 또는 미검증 기능을 구분하고 유효한 경로를 안내한다.
- [ ] 기존 소개와 실행 방법을 유지하고 제외 범위를 변경하지 않았다.
- [ ] 문서 검증과 기본 테스트 결과 및 미검증 사항을 기록했다.
- [ ] PR 리뷰와 devlog를 반영하고 최종 head의 머지 준비 조건을 확인한다.
