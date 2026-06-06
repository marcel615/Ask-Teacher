# Current Task

## 연결 Issue

- Issue 번호: #17
- Issue 제목: refactor: 게시글 목록 조회 내림차순 개선
- Issue URL: https://github.com/marcel615/Ask-Teacher/issues/17

## 작업명

- 게시글 목록 조회 생성일 내림차순 정렬 적용

## 작업 배경

- 현재 게시글 목록 조회는 반환 순서가 명시되어 있지 않거나 보장되지 않을 수 있다.
- 사용자는 게시글 목록에서 최근에 생성된 게시글을 먼저 확인할 수 있어야 한다.
- 이번 Issue에서는 기존 `GET /api/posts` 응답 구조를 유지하면서 `createdAt` 기준 내림차순 정렬만 적용한다.
- 삭제된 게시글 제외 조건은 기존 동작을 유지한다.

## 참조 설계

API / ERD / 요구사항 전체를 복사하지 않는다. 이번 Issue와 직접 관련된 요약과 참조만 적는다.

- 관련 요구사항:
  - 게시글 목록 조회
  - 삭제된 게시글은 목록에서 제외
  - 생성일 내림차순으로 최근 생성 게시글 우선 반환
- 관련 API:
  - `GET /api/posts`
  - 성공 응답: `200 OK`
  - 응답 필드: `postId`, `title`, `writerNickname`, `newPost`, `createdAt`
  - 정렬: `createdAt DESC`
- 관련 Entity:
  - `Post`
  - `Post.createdAt`
  - `Post.deleted`
- 참조 문서:
  - `docs/requirements.md`
  - `docs/api-spec.md`
- ERD 변경 없음:
  - 기존 `Post.createdAt` 필드를 기준으로 정렬하므로 Entity / DB 구조 변경이 필요하지 않다.
  - 신규 테이블, 컬럼, 관계, 제약조건 변경이 없다.

## 작업 브랜치

- 기준 브랜치: `develop`
- 작업 브랜치: `feature/issue-17-post-list-sort-desc`
- PR 방향: `feature/issue-17-post-list-sort-desc` -> `develop`

## 이번 PR에서 할 일

- [ ] `GET /api/posts` 목록 조회 시 `createdAt` 기준 내림차순 정렬 적용
- [ ] 삭제되지 않은 게시글만 조회하는 기존 조건 유지
- [ ] 게시글 목록 조회 응답 구조 유지
- [ ] 생성일 내림차순 정렬 테스트 추가 또는 기존 테스트 보강
- [ ] 수동 API 확인용 `listPosts.http` 확인 또는 필요 시 수정
- [ ] `./gradlew test` 통과 확인

## 이번 PR에서 하지 않을 일

- 검색 조건 추가
- 페이징 추가
- 정렬 파라미터 추가
- 응답 필드 변경
- 게시글 작성/상세/수정/삭제 API 변경
- 인증/인가 구조 변경
- DB 구조 변경
- ERD 변경

## 완료 조건

- [ ] 게시글 목록이 `createdAt` 내림차순으로 반환된다.
- [ ] 삭제된 게시글은 기존처럼 목록에서 제외된다.
- [ ] 게시글 목록 조회 응답 필드가 기존과 동일하다.
- [ ] 정렬 검증 테스트가 통과한다.
- [ ] 수동 API 확인 완료 또는 생략 사유 기록
- [ ] `./gradlew test` 통과
- [ ] git diff 검토
- [ ] PR 본문 작성

## 예상 변경 파일

### Architect 사전 반영 문서

- `docs/current-task.md`
- `docs/requirements.md`
- `docs/api-spec.md`

### ERD 변경 없음

- `docs/erd.md`는 수정하지 않는다.
- 사유: 기존 `Post.createdAt` 필드로 생성일 내림차순 정렬이 가능하며 Entity / DB 구조 변경이 필요하지 않다.

### Builder 구현 변경 예상 파일

- `src/main/java/com/github/marcel615/askteacher/domain/post/repository/PostRepository.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/service/PostService.java`
- `src/test/java/com/github/marcel615/askteacher/domain/post/service/PostServiceTest.java`
- `src/main/java/com/github/marcel615/askteacher/http/post/listPosts.http`
