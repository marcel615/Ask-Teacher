# Current Work Log

## 2026-11-10 — 공통 명세 공식 원본을 Specs main으로 전환

- 사용자 요청과 예외 승인: 이번 문서 정비는 Issue 없이 수행한다. 기존 develop의 application.yaml 미커밋 변경을 보존하기 위해 별도 worktree `.worktrees/specs-source`, 브랜치 `feature/specs-source`에서 작업하며 PR base는 develop이다. 기존 Issue 기반 개발 절차 자체는 유지한다.
- 이전 Issue #58의 PR #72 merge와 월별 devlog 반영을 Main이 확인한 뒤 임시 기록을 전환했다. Main의 설계 게이트를 통과한 `docs/current-task.md` 범위에서 진행했다.
- 공식 원본은 marcel615/Ask-Teacher-specs main의 요구사항·API·ERD다. Main이 commit `79fadfa8c687681186f0c0e1ef0e6e5e1dded33c`의 세 경로 접근과 Backend 사본의 CRLF 정규화 후 내용 동일성을 확인했다. 명세 내용 변경과 Specs 저장소 수정은 하지 않는다.
- AGENTS, 세 Skill, README, Architect 역할 설정, Issue·PR 템플릿, 빠른 지시문과 리뷰 체크리스트를 Specs main 참조로 통일했다. README의 API·ERD는 개요로 구분하고 상세 공식 원본을 연결했다. Architect의 로컬 명세 수정 안내를 제거하고 current-task 설계 작성·독립 리뷰 역할을 유지했다.
- 참조 전환과 내용 동일성 검증 후 Backend 중복 세 문서를 삭제했다. 역사 devlog의 기존 기록과 제품 코드·테스트·설정은 수정하지 않았다.
- 검증: 숨김 디렉터리를 포함한 전체 추적 파일 검색에서 역사 기록·삭제 설명을 제외한 활성 로컬 명세 참조 0건을 확인했다. 신규 Markdown 링크는 모두 Specs main의 검증된 세 경로다. 삭제한 세 파일의 부재와 `git diff --check` 통과를 확인했다.
- 구문 검증: 캐시된 SnakeYAML 2.6, Tomlj 1.0.0과 ANTLR runtime 4.7.2를 사용해 변경한 Issue YAML 및 Architect TOML을 실제 파싱했고 모두 통과했다. 검증용 Java 파일은 ignored build/tmp에만 두었다.
- 전체 테스트: Corretto 17.0.21에서 `.\gradlew.bat test` 성공. JUnit XML 합계 175건, 실패·오류·건너뜀 모두 0건이다. H2 및 Main이 실행한 테스트 전용 Redis(127.0.0.1:16379)를 사용했으며 개발 Redis와 원 작업 트리의 미커밋 설정은 건드리지 않았다. 제품 동작 변경이 없어 새 테스트는 추가하지 않았다.
- 사용자 추가 지시로 이번 작업의 독립 리뷰·devlog 작성·CI 확인을 생략한다. 기존 워크플로우 규칙 자체는 변경하지 않는다. Main은 PR을 생성하며 merge는 사용자가 직접 한다. 테스트용 Redis 컨테이너는 전체 테스트 후 종료·제거했다. 추가 기술 후속 Issue나 기능 후보가 필요한 변경은 확인되지 않았다.
