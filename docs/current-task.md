# Current Task

## 연결 Issue

- Issue 번호: #13
- Issue 제목: [Feature] (post): 게시글 상세 조회
- Issue URL: https://github.com/marcel615/Ask-Teacher/issues/13

## 작업명

- 게시글 상세 조회 API 구현

## 작업 배경

- 사용자는 특정 게시글의 제목과 질문 내용을 상세 조회할 수 있어야 한다.
- 서버는 path variable로 받은 게시글 ID로 Post를 조회한다.
- 조회된 Post를 상세 조회 응답 DTO로 변환해 공통 응답 형식으로 반환한다.
- 응답에는 작성자 이름, 카테고리 이름, 제목, 내용, 생성일시가 포함된다.

## 참조 설계

API / ERD / 요구사항 전체를 복사하지 않는다. 이번 Issue와 직접 관련된 요약과 참조만 적는다.

- 관련 요구사항:
  - 게시글 상세 조회
  - 게시글 ID로 상세 정보 조회
  - 존재하지 않는 게시글은 조회 불가
- 관련 API:
  - `GET /api/posts/{postId}`
  - 성공 응답 형식: `status/message/data`
  - 응답 data: `userName`, `categoryName`, `title`, `content`, `createdAt`
- 관련 Entity:
  - `Post`
  - `User`
  - `Category`
- 참조 문서:
  - `docs/requirements.md`
  - `docs/api-spec.md`
- ERD 변경 없음:
  - 기존 Post, User, Category 관계와 필드만 사용하므로 DB 구조 변경이 필요하지 않다.

## 작업 브랜치

- 기준 브랜치: `develop`
- 작업 브랜치: `feature/issue-13-post-detail`
- PR 방향: `feature/issue-13-post-detail` -> `develop`

## 이번 PR에서 할 일

- [ ] `GET /api/posts/{postId}` API 추가
- [ ] 게시글 ID로 Post 조회
- [ ] 존재하지 않는 게시글 조회 시 예외 처리
- [ ] 게시글 상세 응답 DTO 추가
- [ ] 응답에 작성자 이름, 카테고리 이름, 제목, 내용, 생성일시 포함
- [ ] 공통 응답 형식으로 반환
- [ ] 게시글 상세 조회 테스트 추가
- [ ] 새 API 확인용 `.http` 파일 추가 또는 기존 파일 수정
- [ ] `./gradlew test` 통과 확인

## 이번 PR에서 하지 않을 일

- 게시글 목록 조회 변경
- 게시글 작성/수정/삭제 변경
- 검색, 페이징, 정렬 조건 추가
- 댓글, 좋아요, 파일 업로드 기능
- 로그인 토큰 기반 인증/인가 구조 변경
- DB 구조 변경

## 완료 조건

- [ ] 기능 구현 완료
- [ ] 게시글 조회 성공 테스트 통과
- [ ] 게시글 없음 예외 테스트 통과
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
- 사유: Issue #13은 Entity / DB 변경 없음으로 명시되어 있고, 기존 Post, User, Category 구조만 사용한다.

### Builder 구현 변경 예상 파일

- `src/main/java/com/github/marcel615/askteacher/domain/post/controller/PostController.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/service/PostService.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/dto/PostDetailResponse.java`
- 게시글 상세 조회 관련 테스트 파일
- `src/main/java/com/github/marcel615/askteacher/http` 하위 `.http` 파일
