# ERD / Entity 설계

## Entity 목록

| Entity | 테이블 | 설명 |
|---|---|---|
| User | users | 서비스 사용자 |
| Category | categories | 게시글 카테고리 |
| Post | posts | 질문 게시글 |
| PostLike | post_likes | 게시글 좋아요 |
| PostFile | post_files | 게시글 첨부파일 |
| Comment | comments | 게시글 댓글 |
| CommentLike | comment_likes | 댓글 좋아요 |

## User

| 필드 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| id | Long | PK, AUTO_INCREMENT | 사용자 ID |
| email | String(255) | NOT NULL, UNIQUE | 이메일 |
| password | String(255) | NOT NULL | 암호화된 비밀번호 |
| nickname | String(50) | NOT NULL, UNIQUE | 닉네임 |
| role | UserRole | NOT NULL | 사용자 권한 |
| createdAt | LocalDateTime | NOT NULL | 생성일 |
| updatedAt | LocalDateTime | NOT NULL | 수정일 |

## Category

| 필드 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| id | Long | PK, AUTO_INCREMENT | 카테고리 ID |
| name | String(50) | NOT NULL, UNIQUE | 카테고리명 |
| createdAt | LocalDateTime | NOT NULL | 생성일 |
| updatedAt | LocalDateTime | NOT NULL | 수정일 |

## Post

| 필드 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| id | Long | PK, AUTO_INCREMENT | 게시글 ID |
| user | User | FK, NOT NULL | 작성자 |
| category | Category | FK, NOT NULL | 카테고리 |
| title | String(255) | NOT NULL | 게시글 제목 |
| content | Text | NOT NULL | 게시글 내용 |
| newPost | boolean | NOT NULL | 새 글 여부 |
| deleted | boolean | NOT NULL | 삭제 여부 |
| likeCount | int | NOT NULL | 좋아요 수 |
| createdAt | LocalDateTime | NOT NULL | 생성일 |
| updatedAt | LocalDateTime | NOT NULL | 수정일 |

## PostLike

| 필드 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| id | Long | PK, AUTO_INCREMENT | 게시글 좋아요 ID |
| post | Post | FK, NOT NULL | 좋아요 대상 게시글 |
| user | User | FK, NOT NULL | 좋아요한 사용자 |
| createdAt | LocalDateTime | NOT NULL | 생성일 |

## PostFile

| 필드 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| id | Long | PK, AUTO_INCREMENT | 게시글 첨부파일 ID |
| post | Post | FK, NOT NULL | 첨부파일이 속한 게시글 |
| originalFileName | String(255) | NOT NULL | 원본 파일명 |
| storedFileName | String(255) | NOT NULL | 저장 파일명 |
| filePath | String(500) | NOT NULL | 파일 저장 경로 또는 접근 경로 |
| contentType | String(100) | NOT NULL | MIME 타입 |
| fileSize | Long | NOT NULL | 파일 크기 |
| createdAt | LocalDateTime | NOT NULL | 생성일 |

## Comment

| 필드 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| id | Long | PK, AUTO_INCREMENT | 댓글 ID |
| post | Post | FK, NOT NULL | 댓글 대상 게시글 |
| user | User | FK, NOT NULL | 댓글 작성자 |
| content | String(1000) | NOT NULL | 댓글 내용 |
| deleted | boolean | NOT NULL, DEFAULT false | 논리 삭제 여부 |
| createdAt | LocalDateTime | NOT NULL | 생성일 |
| updatedAt | LocalDateTime | NOT NULL | 수정일 |

## CommentLike

| 필드 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| id | Long | PK, AUTO_INCREMENT | 댓글 좋아요 ID |
| comment | Comment | FK, NOT NULL | 좋아요 대상 댓글 |
| user | User | FK, NOT NULL | 좋아요한 사용자 |
| createdAt | LocalDateTime | NOT NULL | 생성일 |

## 관계

- User 1 : N Post
  - 한 명의 사용자는 여러 게시글을 작성할 수 있다.
- Category 1 : N Post
  - 하나의 카테고리는 여러 게시글을 가질 수 있다.
- Post 1 : N PostLike
  - 하나의 게시글은 여러 좋아요를 가질 수 있다.
- Post 1 : N PostFile
  - 하나의 게시글은 여러 첨부파일을 가질 수 있다.
- User 1 : N PostLike
  - 한 명의 사용자는 여러 게시글에 좋아요를 누를 수 있다.
- User N : M Post
  - 좋아요 관계는 PostLike를 통해 표현한다.
- Post 1 : N Comment
  - 하나의 게시글은 여러 댓글을 가질 수 있다.
- User 1 : N Comment
  - 한 명의 사용자는 여러 댓글을 작성할 수 있다.
- Comment 1 : N CommentLike
  - 하나의 댓글은 여러 좋아요를 가질 수 있다.
- User 1 : N CommentLike
  - 한 명의 사용자는 여러 댓글에 좋아요를 누를 수 있다.
- User N : M Comment
  - 댓글 좋아요 관계는 CommentLike를 통해 표현한다.

## FK

| 테이블 | 컬럼 | 참조 테이블 | 참조 컬럼 |
|---|---|---|---|
| posts | user_id | users | id |
| posts | category_id | categories | id |
| post_likes | post_id | posts | id |
| post_likes | user_id | users | id |
| post_files | post_id | posts | id |
| comments | post_id | posts | id |
| comments | user_id | users | id |
| comment_likes | comment_id | comments | id |
| comment_likes | user_id | users | id |

## Unique 제약

| 테이블 | 컬럼 | 설명 |
|---|---|---|
| post_likes | post_id, user_id | 같은 사용자는 같은 게시글에 좋아요를 1개만 등록할 수 있다. |
| comment_likes | comment_id, user_id | 같은 사용자는 같은 댓글에 좋아요를 1개만 등록할 수 있다. |

## Index

| 테이블 | 컬럼 | 설명 |
|---|---|---|
| comments | post_id, deleted, created_at, id | 게시글별 미삭제 댓글의 최신순 조회와 동률 정렬 지원 |
| comment_likes | comment_id, user_id | 댓글별 좋아요 집계 및 사용자별 중복 좋아요 방지 |

## Issue #5 관련 조회 조건

- 게시글 목록 조회는 `posts.deleted = false`인 게시글만 반환한다.
- 응답에는 `posts.id`, `posts.title`, `users.nickname`, `posts.newPost`, `posts.createdAt`을 사용한다.

## Issue #27 관련 조회 및 삭제 조건

- 댓글 목록은 `comments.deleted = false`인 댓글만 반환한다.
- 댓글 목록의 최신순 정렬은 `created_at DESC`, `id DESC`를 사용한다.
- 댓글 목록의 좋아요순 정렬은 좋아요 집계값 DESC, `created_at DESC`, `id DESC`를 사용한다.
- 댓글 좋아요 수는 `comment_likes`를 집계하며 `comments`에 별도 개수 컬럼을 두지 않는다.
- 댓글 좋아요 취소 시 해당 `comment_likes` 행을 물리 삭제한다.
- 댓글 삭제 시 `comments.deleted = true`로 변경하고 관련 `comment_likes` 행을 물리 삭제한다.
- 댓글 논리 삭제와 관련 좋아요 삭제는 같은 트랜잭션에서 처리한다.
