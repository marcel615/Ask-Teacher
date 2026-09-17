# Current Task

## Issue

- Issue: #27
- Title: [Feature] comment: 댓글 CRUD·좋아요·정렬 및 페이징
- URL: https://github.com/marcel615/Ask-Teacher/issues/27
- State: OPEN
- Labels: 없음

## 목표

게시글별 댓글 작성·조회·수정·삭제 기능과 댓글 좋아요 등록·취소
기능을 구현한다.

댓글 목록은 게시글 상세 API와 분리하여 제공하고, 비로그인 조회,
페이지 기반 페이징, 최신순·좋아요순 정렬을 지원한다.

## 범위

- 로그인 사용자의 댓글 작성
- 게시글별 댓글 목록 공개 조회
- 댓글 작성자의 댓글 수정 및 논리 삭제
- 댓글 좋아요 등록 및 취소
- 댓글 삭제 시 관련 좋아요 물리 삭제
- 최신순 및 좋아요 개수순 정렬
- 페이지 기반 페이징
- 로그인 사용자의 `likedByMe` 계산
- 비로그인 사용자의 `likedByMe=false` 처리
- 댓글 좋아요 수 실시간 집계
- DTO Validation과 예외 처리
- Controller·Service·Repository 및 API 통합 테스트
- 요구사항, API 명세, ERD 반영

## 제외 범위

- 게시글 상세 API 응답에 댓글 목록 포함
- 대댓글
- 댓글 신고
- 관리자 댓글 삭제
- 댓글 알림
- 댓글 검색
- 삭제된 댓글 복구
- 댓글 좋아요 사용자 목록 조회
- 댓글 좋아요 이력 보존 및 복구
- 최신순·좋아요순 이외 정렬
- 댓글 기능과 관계없는 기존 코드 리팩터링

## 요구사항 변경 요약

- `docs/requirements.md` 변경 필요
- 비로그인 사용자의 댓글 목록 조회 추가
- 로그인 사용자의 댓글 CRUD 및 좋아요 기능 추가
- 댓글 CRUD를 제외하던 기존 제외 범위 제거
- 대댓글, 신고, 알림, 검색, 복구 및 좋아요 이력은 제외
- 참조: `docs/requirements.md`

## API 변경 요약

다음 API를 추가한다.

- `POST /api/posts/{postId}/comments`
- `GET /api/posts/{postId}/comments`
- `PATCH /api/comments/{commentId}`
- `DELETE /api/comments/{commentId}`
- `POST /api/comments/{commentId}/likes`
- `DELETE /api/comments/{commentId}/likes`

상세 요청·응답, Validation 및 상태 코드는 `docs/api-spec.md`를 따른다.

## ERD 변경

- ERD 변경 필요
- 사유:
  - 댓글 저장을 위한 `Comment/comments`가 필요하다.
  - 댓글 좋아요 및 사용자별 중복 방지를 위한
    `CommentLike/comment_likes`가 필요하다.
  - Post·User·Comment·CommentLike 간 FK와 관계가 추가된다.
  - `(comment_id, user_id)` 유니크 제약이 필요하다.
- 참조: `docs/erd.md`

## 도메인 설계

### Comment

- `Post`와 N:1
- `User`와 N:1
- 내용은 필수이며 최대 1,000자
- 생성 시 `deleted=false`
- 수정 시 `updatedAt` 갱신
- 삭제 시 `deleted=true`로 변경
- 삭제된 댓글은 존재하지 않는 댓글과 동일하게 처리

### CommentLike

- `Comment`와 N:1
- `User`와 N:1
- `(comment_id, user_id)` 유니크 제약 적용
- 좋아요 취소 시 행 물리 삭제
- 댓글 삭제 시 해당 댓글의 좋아요 행 일괄 물리 삭제
- 좋아요 수는 `comment_likes` 집계 결과 사용
- `Comment`에는 좋아요 개수 컬럼을 두지 않음

## API 설계

### 댓글 작성

- Method: `POST`
- URL: `/api/posts/{postId}/comments`
- 인증 필요
- 요청: `content`
- 응답: `201 Created`, 댓글 정보
- 인증 사용자 ID를 작성자로 사용
- 존재하고 삭제되지 않은 게시글에만 작성 가능

### 댓글 목록 조회

- Method: `GET`
- URL: `/api/posts/{postId}/comments`
- 비로그인 조회 가능
- Query:
  - `page`: 기본값 0, 0 이상
  - `size`: 기본값 20, 1~100
  - `sort`: 기본값 `latest`, 허용값 `latest`, `likeCount`
- 응답:
  - `comments`
  - `page`
  - `size`
  - `totalElements`
  - `totalPages`
  - `hasNext`
  - `sort`
- 존재하고 삭제되지 않은 게시글만 조회 가능
- 삭제된 댓글 제외
- 비로그인 사용자의 `likedByMe=false`
- 로그인 사용자는 실제 좋아요 여부 반환

정렬:

- `latest`: `createdAt DESC`, `commentId DESC`
- `likeCount`: `likeCount DESC`, `createdAt DESC`, `commentId DESC`

댓글 목록 조회 쿼리는 페이지 내 각 댓글에 대해 개별 집계 쿼리를
반복하지 않고 좋아요 수와 `likedByMe`를 함께 조회하도록 구현한다.

### 댓글 수정

- Method: `PATCH`
- URL: `/api/comments/{commentId}`
- 인증 필요
- 댓글 작성자만 가능
- `200 OK`와 수정된 댓글 정보 반환

### 댓글 삭제

- Method: `DELETE`
- URL: `/api/comments/{commentId}`
- 인증 필요
- 댓글 작성자만 가능
- 댓글 논리 삭제
- 관련 댓글 좋아요 물리 삭제
- `204 No Content`

### 댓글 좋아요 등록

- Method: `POST`
- URL: `/api/comments/{commentId}/likes`
- 인증 필요
- 중복 등록 불가
- `201 Created`
- 등록 이후 `commentId`, `likeCount`, `likedByMe=true` 반환

서비스 사전 중복 확인과 DB 유니크 제약을 모두 적용한다.
동시 중복 요청의 DB 제약 위반도 `409 Conflict`로 변환한다.

### 댓글 좋아요 취소

- Method: `DELETE`
- URL: `/api/comments/{commentId}/likes`
- 인증 필요
- 좋아요 행 물리 삭제
- 취소 대상이 없으면 `404 Not Found`
- `200 OK`
- 취소 이후 `commentId`, `likeCount`, `likedByMe=false` 반환

## Validation

- `content`: 필수, 공백만 입력 불가, 최대 1,000자
- 저장 전 `content` 앞뒤 공백 제거
- `page`: 0 이상
- `size`: 1 이상 100 이하
- `sort`: `latest`, `likeCount`만 허용
- 인증 사용자 ID를 요청 본문으로 받지 않음

## 예외 처리

추가할 오류 코드:

- `COMMENT_NOT_FOUND`: 404
- `COMMENT_AUTHOR_MISMATCH`: 403
- `DUPLICATE_COMMENT_LIKE`: 409
- `COMMENT_LIKE_NOT_FOUND`: 404

기존 오류 코드 사용:

- `POST_NOT_FOUND`: 404
- `USER_NOT_FOUND`: 404
- `UNAUTHORIZED`: 401
- `INVALID_INPUT_VALUE`: 400

`GlobalExceptionHandler`는 `CustomException`과 `ErrorCode`를 공통 처리하므로
새 처리 방식이 필요하지 않는 한 수정하지 않는다.

## 인증 및 공개 조회

- `GET /api/posts/{postId}/comments`를 `permitAll`로 설정한다.
- Access Token이 있으면 인증 사용자 ID를 이용해 `likedByMe`를 계산한다.
- 인증 정보가 없으면 `likedByMe=false`로 반환한다.
- 댓글 작성·수정·삭제 및 좋아요 등록·취소는 인증이 필요하다.

## 트랜잭션

- 댓글 작성·수정·삭제는 트랜잭션으로 처리한다.
- 댓글 삭제와 관련 좋아요 삭제는 같은 트랜잭션으로 처리한다.
- 좋아요 등록·취소와 변경 후 개수 집계는 같은 트랜잭션으로 처리한다.
- 좋아요 중복은 애플리케이션 검증과 DB 유니크 제약으로 방지한다.

## 테스트 및 검증

### Controller 테스트

- 요청 바인딩과 Validation
- 기본 페이징·정렬값
- 댓글별 성공 상태와 응답 필드
- 인증 필요 API의 401
- 비로그인 목록 조회
- 정의된 오류별 HTTP 상태

### Service 테스트

- 댓글 작성·수정·삭제
- 게시글 및 댓글 존재 검증
- 댓글 작성자 권한 검증
- 댓글 논리 삭제
- 댓글 삭제 시 좋아요 물리 삭제
- 좋아요 등록·중복 등록·취소
- 삭제된 댓글 접근 차단

### Repository 테스트

- 삭제되지 않은 댓글만 조회
- 최신순과 동률 정렬
- 좋아요 집계와 좋아요순 정렬
- 페이지 조회와 전체 개수
- 로그인 사용자의 `likedByMe`
- `(comment_id, user_id)` 유니크 제약
- 댓글별 좋아요 물리 삭제

### API 통합 테스트

- 인증된 댓글 작성·수정·삭제 흐름
- 비로그인 댓글 목록 조회
- 로그인 사용자의 `likedByMe`
- 정렬과 페이징
- Validation 실패
- 작성자 권한 실패
- 좋아요 등록·중복·취소
- 삭제 댓글 제외 및 접근 실패
- 테스트 DB 및 인증 환경 격리
- 테스트 데이터 정리

기본 검증:

`.\gradlew.bat test`

테스트 대상, 실행 결과 및 미검증 사항을
`docs/current-work-log.md`에 기록한다.

## 예상 변경 파일

### Architect 사전 반영 문서

- `docs/current-task.md`
  - Issue #27의 확정된 목표, 범위, 설계 및 완료 조건 반영
- `docs/requirements.md`
  - 댓글 및 댓글 좋아요 요구사항과 제외 범위 반영
- `docs/api-spec.md`
  - 댓글 관련 API 6개와 요청·응답·상태 코드 반영
- `docs/erd.md`
  - Comment, CommentLike, 관계, FK 및 유니크 제약 반영
- `docs/prompts/quick.md`
  - Issue 기반 역할별 빠른 지시문에 자동 API 테스트 기준 반영

위 다섯 문서는 이번 초안 승인 후 Architect가 사전 반영한다.

### Builder 구현 변경 예상 파일

신규 애플리케이션 파일:

- `src/main/java/com/github/marcel615/askteacher/domain/comment/entity/Comment.java`
- `src/main/java/com/github/marcel615/askteacher/domain/comment/repository/CommentRepository.java`
- `src/main/java/com/github/marcel615/askteacher/domain/comment/service/CommentService.java`
- `src/main/java/com/github/marcel615/askteacher/domain/comment/controller/CommentController.java`
- `src/main/java/com/github/marcel615/askteacher/domain/comment/type/CommentSort.java`
- `src/main/java/com/github/marcel615/askteacher/domain/comment/dto/CommentCreateRequest.java`
- `src/main/java/com/github/marcel615/askteacher/domain/comment/dto/CommentUpdateRequest.java`
- `src/main/java/com/github/marcel615/askteacher/domain/comment/dto/CommentResponse.java`
- `src/main/java/com/github/marcel615/askteacher/domain/comment/dto/CommentPageResponse.java`
- `src/main/java/com/github/marcel615/askteacher/domain/commentlike/entity/CommentLike.java`
- `src/main/java/com/github/marcel615/askteacher/domain/commentlike/repository/CommentLikeRepository.java`
- `src/main/java/com/github/marcel615/askteacher/domain/commentlike/service/CommentLikeService.java`
- `src/main/java/com/github/marcel615/askteacher/domain/commentlike/controller/CommentLikeController.java`
- `src/main/java/com/github/marcel615/askteacher/domain/commentlike/dto/CommentLikeResponse.java`

기존 애플리케이션 수정 예상 파일:

- `src/main/java/com/github/marcel615/askteacher/global/config/SecurityConfig.java`
  - 댓글 목록 GET 공개
- `src/main/java/com/github/marcel615/askteacher/global/exception/ErrorCode.java`
  - 댓글 관련 403·404·409 오류 코드 추가

신규 테스트 파일:

- `src/test/java/com/github/marcel615/askteacher/domain/comment/controller/CommentControllerTest.java`
- `src/test/java/com/github/marcel615/askteacher/domain/comment/service/CommentServiceTest.java`
- `src/test/java/com/github/marcel615/askteacher/domain/comment/repository/CommentRepositoryTest.java`
- `src/test/java/com/github/marcel615/askteacher/domain/commentlike/controller/CommentLikeControllerTest.java`
- `src/test/java/com/github/marcel615/askteacher/domain/commentlike/service/CommentLikeServiceTest.java`
- `src/test/java/com/github/marcel615/askteacher/domain/commentlike/repository/CommentLikeRepositoryTest.java`
- `src/test/java/com/github/marcel615/askteacher/integration/CommentApiIntegrationTest.java`

기존 테스트 수정 예상 파일:

- `src/test/java/com/github/marcel615/askteacher/global/config/SecurityConfigTest.java`
  - 댓글 목록 공개 및 변경 API 인증 검증

작업 기록:

- `docs/current-work-log.md`
  - Issue #27 기준으로 초기화
  - 주요 결정, 테스트 결과 및 미검증 사항 기록

JPA `ddl-auto`가 개발 환경에서 `create`, 테스트 환경에서 `create-drop`이므로
현재 저장소 구조상 별도 DB migration 파일은 예상하지 않는다.

위 파일 외 변경이 필요하면 Builder가 사유와 범위를 먼저 보고하고
사용자 승인을 받은 후 진행한다.

## 구현 순서

1. Comment 및 CommentLike Entity와 Repository 구현
2. 댓글 DTO와 정렬 타입 구현
3. 댓글 Service 구현
4. 댓글 좋아요 Service 구현
5. Controller와 공개 조회 보안 설정 구현
6. 오류 코드 및 예외 매핑 적용
7. 계층별 테스트 작성
8. API 통합 테스트 작성
9. 전체 테스트 실행
10. 결과와 미검증 사항 기록

## 완료 조건

- [ ] 댓글 관련 API 6개가 명세대로 동작한다.
- [ ] 댓글 목록은 비로그인 상태에서 조회할 수 있다.
- [ ] 댓글 목록의 정렬·페이징·`likedByMe`가 명세와 일치한다.
- [ ] 댓글 수정·삭제는 작성자만 가능하다.
- [ ] 댓글은 논리 삭제되고 목록에서 제외된다.
- [ ] 댓글 삭제 시 관련 좋아요가 물리 삭제된다.
- [ ] 중복 좋아요는 DB 제약을 포함해 `409`로 처리된다.
- [ ] 좋아요하지 않은 상태의 취소는 `404`로 처리된다.
- [ ] Controller·Service·Repository 테스트가 작성되어 있다.
- [ ] 인증부터 DB 처리까지 API 통합 테스트가 작성되어 있다.
- [ ] `.\gradlew.bat test`가 통과한다.
- [ ] 테스트 범위, 실행 결과 및 미검증 사항이 기록되어 있다.
