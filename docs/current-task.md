# Current Task

## 연결 Issue

- Issue 번호: #5
- Issue 제목: feat(post): 게시글 목록 조회
- Issue URL: https://github.com/marcel615/Ask-Teacher/issues/5

## 작업명

- 게시글 목록 조회 API 구현

## 작업 배경

- 사용자는 게시글 목록을 조회할 수 있어야 한다.
- 게시글 목록에는 게시글 ID, 제목, 작성자 닉네임, 새 글 여부, 생성일이 포함되어야 한다.
- 삭제된 게시글은 목록 조회 결과에서 제외되어야 한다.

## 참조 설계

API / ERD / 요구사항 전체는 복사하지 않는다. 이번 Issue와 직접 관련된 요약과 참조만 적는다.

- 관련 요구사항:
  - 게시글 목록 조회
  - 삭제된 게시글 제외
- 관련 API:
  - `GET /api/posts`
  - 성공 응답 형식: `status/message/data`
- 관련 Entity:
  - `Post`
  - `User`
  - `Category`
- 참조 문서:
  - `docs/requirements.md`
  - `docs/api-spec.md`
  - `docs/erd.md`

## 작업 브랜치

- 기준 브랜치: `develop`
- 작업 브랜치: `feature/issue-5-post-list`
- PR 방향: `feature/issue-5-post-list` → `develop`

## 이번 PR에서 할 일

- [ ] `GET /api/posts` API 추가
- [ ] 게시글 목록 응답 DTO 추가
- [ ] 삭제되지 않은 게시글만 조회
- [ ] 응답에 `postId`, `title`, `writerNickname`, `isNew`, `createdAt` 포함
- [ ] 게시글 목록 조회 테스트 추가 또는 기존 테스트 보강
- [ ] `./gradlew test` 통과 확인

## 이번 PR에서 하지 않을 일

- 게시글 상세 조회
- 게시글 수정
- 게시글 삭제
- 검색 / 페이징 / 정렬 조건
- 댓글 기능
- 인증 / 인가 구조 변경
- DB 구조 변경

## 완료 조건

- [ ] 기능 구현 완료
- [ ] Validation / 예외 처리 확인
- [ ] 테스트 통과
- [ ] git diff 검토
- [ ] PR 본문 작성

## 예상 변경 파일

### 생성 가능

- `src/main/java/com/github/marcel615/askteacher/domain/post/dto/PostListResponse.java`

### 수정 가능

- `src/main/java/com/github/marcel615/askteacher/domain/post/controller/PostController.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/service/PostService.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/repository/PostRepository.java`
- 게시글 목록 조회 관련 테스트 파일
