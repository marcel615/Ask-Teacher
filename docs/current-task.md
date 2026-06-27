# Current Task

## Related Issue

- Issue number: #24
- Issue title: [Feature] post: 게시글 좋아요 기능
- Issue URL: https://github.com/marcel615/Ask-Teacher/issues/24

## Task

- 게시글 좋아요 등록/취소 기능을 구현한다.
- 게시글 목록/상세 조회 응답에 좋아요 정보를 추가한다.
- 좋아요 저장을 위한 `post_likes` 테이블과 `posts.likeCount` 필드를 추가한다.

## Assumptions

- `POST /api/posts/{postId}/likes`는 좋아요 등록만 수행한다.
- 이미 좋아요한 게시글에 `POST`를 다시 요청하면 `400 Bad Request`로 처리한다.
- 좋아요 취소는 `DELETE /api/posts/{postId}/likes`로 수행한다.
- 삭제된 게시글은 존재하지 않는 게시글과 동일하게 보고 좋아요 등록/취소 대상에서 제외한다.
- `posts.likeCount`는 조회 성능을 위한 카운터 필드이며, `post_likes`가 사용자별 좋아요 여부의 기준 데이터다.
- Issue 요청에 따라 이번 작업의 검증은 자동 테스트 추가 없이 `.http` 수동 API 확인을 우선한다.

## Scope

- 로그인 사용자의 게시글 좋아요 등록 API를 추가한다.
- 로그인 사용자의 게시글 좋아요 취소 API를 추가한다.
- 사용자당 게시글 1개에 좋아요 1개만 등록되도록 제한한다.
- 존재하지 않거나 삭제된 게시글에는 좋아요를 등록/취소할 수 없도록 처리한다.
- 중복 좋아요 등록 요청은 `400 Bad Request`로 처리한다.
- 좋아요하지 않은 게시글의 좋아요 취소 요청은 `400 Bad Request`로 처리한다.
- 게시글 목록 조회 응답 DTO에 `likeCount`를 추가한다.
- 게시글 상세 조회 응답 DTO에 `likeCount`, `likedByMe`를 추가한다.
- 좋아요 등록/취소 시 `posts.likeCount`를 갱신한다.
- 수동 API 확인용 `.http` 파일을 추가 또는 수정한다.

## Out Of Scope

- 댓글 기능
- 알림 기능
- 좋아요 사용자 목록 조회
- 좋아요 기반 정렬/검색/페이징
- 프론트엔드 구현
- 고도화된 권한 처리
- 자동 테스트 추가

## API Summary

### 게시글 좋아요 등록

- Method: `POST`
- URL: `/api/posts/{postId}/likes`
- Auth: 필요
- Success: `200 OK`
- Errors:
  - `401 Unauthorized`: 인증되지 않은 사용자
  - `404 Not Found`: 게시글 없음
  - `400 Bad Request`: 이미 좋아요한 게시글

### 게시글 좋아요 취소

- Method: `DELETE`
- URL: `/api/posts/{postId}/likes`
- Auth: 필요
- Success: `200 OK`
- Errors:
  - `401 Unauthorized`: 인증되지 않은 사용자
  - `404 Not Found`: 게시글 없음
  - `400 Bad Request`: 좋아요 취소 대상 없음

### 게시글 목록 조회 응답 변경

- 기존 응답 DTO에 `likeCount` 필드를 추가한다.

### 게시글 상세 조회 응답 변경

- 기존 응답 DTO에 `likeCount` 필드를 추가한다.
- 기존 응답 DTO에 `likedByMe` 필드를 추가한다.

## ERD Summary

- ERD 변경 필요.
- `post_likes` 테이블을 추가한다.
- `post_likes.post_id`는 `posts.id`를 참조한다.
- `post_likes.user_id`는 `users.id`를 참조한다.
- `(post_id, user_id)` 유니크 제약을 추가한다.
- `posts` 테이블에 `likeCount` 필드를 추가한다.

## Branch

- Base branch: `develop`
- Working branch: `feature/issue-24-post-like`
- PR direction: `feature/issue-24-post-like` -> `develop`

## Completion Criteria

- [ ] 로그인 사용자는 게시글 좋아요를 등록할 수 있다.
- [ ] 로그인 사용자는 자신이 등록한 게시글 좋아요를 취소할 수 있다.
- [ ] 인증되지 않은 사용자의 좋아요 등록/취소 요청은 `401 Unauthorized`로 실패한다.
- [ ] 존재하지 않거나 삭제된 게시글에 대한 좋아요 등록/취소 요청은 `404 Not Found`로 실패한다.
- [ ] 이미 좋아요한 게시글에 대한 중복 등록 요청은 `400 Bad Request`로 실패한다.
- [ ] 좋아요하지 않은 게시글에 대한 취소 요청은 `400 Bad Request`로 실패한다.
- [ ] 같은 사용자와 같은 게시글 조합의 좋아요는 1개만 저장된다.
- [ ] 게시글 목록 조회 응답에 `likeCount`가 포함된다.
- [ ] 게시글 상세 조회 응답에 `likeCount`, `likedByMe`가 포함된다.
- [ ] 좋아요 등록/취소 시 `posts.likeCount`가 일관되게 갱신된다.
- [ ] 관련 `.http` 파일로 수동 API 확인이 가능하다.
- [ ] 변경 문서와 구현 범위가 Issue #24를 벗어나지 않는다.

## Expected Files

### Architect 사전 반영 문서

- `docs/current-task.md`
- `docs/requirements.md`
- `docs/api-spec.md`
- `docs/erd.md`

### Builder 구현 변경 예상 파일

- `src/main/java/com/github/marcel615/askteacher/domain/post/entity/Post.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/repository/PostRepository.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/service/PostService.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/controller/PostController.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/dto/*.java`
- `src/main/java/com/github/marcel615/askteacher/domain/postlike/entity/PostLike.java`
- `src/main/java/com/github/marcel615/askteacher/domain/postlike/repository/PostLikeRepository.java`
- `src/main/java/com/github/marcel615/askteacher/domain/postlike/service/PostLikeService.java`
- `src/main/java/com/github/marcel615/askteacher/domain/postlike/controller/PostLikeController.java`
- DB migration or schema initialization files if this project uses them
- `src/main/java/com/github/marcel615/askteacher/http/*.http`
