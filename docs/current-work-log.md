# Current Work Log

## Issue #27 — 댓글 CRUD·좋아요·정렬 및 페이징

- 승인된 Architect 사전 문서 변경 5건을 보존한 채 `develop`과 `origin/develop`의 차이가 0/0임을 확인했다.
- `feature/issue-27-comment-crud-likes` 브랜치를 생성하고 이전 Issue #34 임시 기록을 현재 Issue 기준으로 초기화했다.
- 최초 `git fetch origin`은 샌드박스의 `.git` 쓰기 제한으로 실패했으며, 승인된 권한으로 재실행하여 성공했다.

## 주요 결정 및 중간 검증

- 댓글 목록은 페이지 내 댓글별 추가 쿼리를 피하기 위해 `Comment`와 `CommentLike`를 조인·그룹화하여 좋아요 수와 `likedByMe`를 함께 투영한다.
- 최초 타깃 테스트에서 좋아요 일괄 삭제 쿼리의 `clearAutomatically=true`가 관리 중인 댓글 엔티티를 분리해 논리 삭제가 반영되지 않는 동작을 확인했다. 자동 clear를 제거해 좋아요 물리 삭제와 댓글 논리 삭제가 같은 트랜잭션에서 정상 반영되도록 수정했다.

## 테스트 및 미검증 사항

- 최초 댓글·보안 타깃 테스트: 23개 실행, 22개 성공, 1개 실패. 댓글 삭제 후 `deleted=true`가 저장되지 않는 트랜잭션 문제를 발견해 수정했다.
- 최초 샌드박스 테스트 실행은 Gradle 배포본 다운로드 네트워크 제한으로 실패했고, 승인된 네트워크 권한으로 재실행했다.
- 수정 후 댓글 Controller·Service·Repository, 댓글 좋아요 Controller·Service·Repository, API 통합 및 보안 타깃 테스트 23개가 모두 통과했다.
- `.\gradlew.bat test`: 전체 125개 실행, 실패 0, 오류 0, skip 0, `BUILD SUCCESSFUL`.
- 예상 범위 밖 `PostRepository` 변경을 피하도록 기존 조회 메서드를 재사용한 뒤 전체 테스트를 재실행했고 동일하게 통과했다.
- Controller 테스트는 JSON 바인딩·Validation·기본 페이징/정렬·응답 상태/필드·공개 조회·비즈니스 예외를 검증했다.
- Service 테스트는 작성·조회·수정·논리 삭제·작성자 검증·삭제 댓글 차단·좋아요 중복/취소·삭제 시 좋아요 제거를 검증했다.
- Repository 테스트는 삭제 댓글 제외, 최신순/좋아요순 동률 정렬, 페이징/전체 개수, 좋아요 집계, `likedByMe`, 유니크 제약 및 일괄 삭제를 H2에서 검증했다.
- API 통합 테스트는 JWT 인증을 포함한 댓글 CRUD, 공개 목록, `likedByMe`, 정렬·페이징, Validation, 권한 실패, 좋아요 등록·중복·취소 및 삭제 댓글 접근 실패를 검증했다.
- 테스트 프로필의 H2와 격리된 애플리케이션 컨텍스트를 사용하고, 통합 테스트마다 댓글 좋아요→댓글→기존 데이터 순으로 정리했다.
- MySQL에서의 JPQL 실행 계획·성능과 실제 동시 중복 좋아요 경합은 미검증이다. 중복 경합은 DB 유니크 제약 및 `DataIntegrityViolationException` 변환 단위 테스트로 검증했다.
- `git diff --check`가 통과했다.
