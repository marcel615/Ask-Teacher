# Current Task

## 연결 Issue

- Issue 번호: #14
- Issue 제목: [Feature] (post): 게시글 삭제
- Issue URL: https://github.com/marcel615/Ask-Teacher/issues/14

## 작업명

- 게시글 삭제 API 구현

## 작업 배경

- 사용자는 특정 질문 게시글을 삭제할 수 있어야 한다.
- 서버는 path variable로 받은 게시글 ID로 Post를 조회한 뒤 삭제 처리한다.
- 이번 Issue에서는 작성자 검증과 로그인 토큰 기반 인증/인가 구조 변경을 제외한다.
- 삭제는 DB row를 제거하지 않고 기존 `Post.deleted` 값을 `true`로 변경하는 soft delete 방식으로 처리한다.
- 존재하지 않거나 이미 삭제된 게시글은 삭제할 수 없다.
- 삭제 성공 시 기존 공통 응답 구조에 맞춰 `200 OK`와 `status/message`를 반환하고 `data`는 포함하지 않는다.

## 참조 설계

API / ERD / 요구사항 전체를 복사하지 않는다. 이번 Issue와 직접 관련된 요약과 참조만 적는다.

- 관련 요구사항:
  - 게시글 삭제
  - 게시글 ID로 게시글 삭제 요청
  - 작성자 검증은 추후 인증/인가 리팩토링 때 보완
  - 존재하지 않는 게시글 삭제 불가
  - 이미 삭제된 게시글은 존재하지 않는 게시글과 동일하게 처리
- 관련 API:
  - `DELETE /api/posts/{postId}`
  - 성공 응답 형식: `status/message`
  - 삭제 성공: `200 OK`
  - 게시글 없음: `404 Not Found`
- 관련 Entity:
  - `Post`
  - `Post.deleted`
  - `Post.updatedAt`
- 참조 문서:
  - `docs/requirements.md`
  - `docs/api-spec.md`
- ERD 변경 없음:
  - 기존 `Post.deleted` 필드를 사용하므로 DB 구조 변경이 필요하지 않다.
  - Issue #14에서도 Entity / DB 변경은 필요 없다고 명시되어 있다.

## 작업 브랜치

- 기준 브랜치: `develop`
- 작업 브랜치: `feature/issue-14-post-delete`
- PR 방향: `feature/issue-14-post-delete` -> `develop`

## 이번 PR에서 할 일

- [ ] `DELETE /api/posts/{postId}` API 추가
- [ ] 게시글 ID로 Post 조회
- [ ] 존재하지 않는 게시글 삭제 요청 시 예외 처리
- [ ] 이미 삭제된 게시글 삭제 요청 시 404 Not Found 처리
- [ ] 게시글 삭제 시 `deleted = true`로 변경
- [ ] 게시글 삭제 시 `updatedAt` 갱신
- [ ] 삭제 성공 시 `200 OK`와 `status/message` 반환
- [ ] 게시글 삭제 테스트 추가
- [ ] 새 API 확인용 `.http` 파일 추가 또는 기존 파일 수정
- [ ] `./gradlew test` 통과 확인

## 이번 PR에서 하지 않을 일

- 작성자 검증
- 로그인 토큰 기반 인증/인가 구조 변경
- 게시글 목록 조회 변경
- 게시글 상세 조회 변경
- 게시글 작성/수정 변경
- 댓글, 좋아요, 파일 업로드 기능
- DB 구조 변경
- 물리 삭제

## 완료 조건

- [ ] 기능 구현 완료
- [ ] 삭제 성공 테스트 통과
- [ ] 게시글 없음 예외 테스트 통과
- [ ] 이미 삭제된 게시글 예외 테스트 통과
- [ ] 수동 API 확인 완료 또는 생략 사유 기록
- [ ] `./gradlew test` 통과
- [ ] git diff 검토
- [ ] PR 본문 작성

## 예상 변경 파일

### Architect 사전 반영 문서

- `docs/current-task.md`
- `docs/requirements.md`
- `docs/api-spec.md`
- `docs/prompts/quick.md`

### ERD 변경 없음

- `docs/erd.md`는 수정하지 않는다.
- 사유: 기존 `Post.deleted` 필드로 soft delete 처리가 가능하며 Entity / DB 구조 변경이 필요하지 않다.

### Builder 구현 변경 예상 파일

- `src/main/java/com/github/marcel615/askteacher/domain/post/controller/PostController.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/service/PostService.java`
- 게시글 삭제 관련 테스트 파일
- `src/main/java/com/github/marcel615/askteacher/http` 하위 `.http` 파일
