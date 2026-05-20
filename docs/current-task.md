# Current Task

## 연결 Issue

- Issue 번호: #8
- Issue 제목: feat(post): 게시글 수정
- Issue URL: https://github.com/marcel615/Ask-Teacher/issues/8

## 작업명

- 게시글 수정 API 구현

## 작업 배경

- 사용자는 기존 게시글의 카테고리, 제목, 내용을 수정할 수 있어야 한다.
- 게시글 수정 요청은 게시글 ID를 path variable로 받는다.
- 로그인/토큰 기반 인증/인가 구조가 아직 정식 범위가 아니므로, request body의 사용자 ID로 작성자 여부를 검증한다.
- 수정 성공 시 수정된 게시글 정보와 updatedAt을 반환한다.

## 참조 설계

API / ERD / 요구사항 전체는 복사하지 않는다. 이번 Issue와 직접 관련된 요약과 참조만 적는다.

- 관련 요구사항:
  - 게시글 수정
  - 요청 사용자 ID가 게시글 작성자 ID와 일치할 때만 수정 가능
  - 제목 필수, 100자 이하
  - 내용 필수, 5000자 이하
  - 존재하는 사용자, 게시글, 카테고리만 수정 가능
- 관련 API:
  - `PATCH /api/posts/{postId}`
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
- 작업 브랜치: `feature/issue-8-post-update`
- PR 방향: `feature/issue-8-post-update` → `develop`

## 이번 PR에서 할 일

- [ ] `PATCH /api/posts/{postId}` API 추가
- [ ] 게시글 수정 요청 DTO에 `userId`, `categoryId`, `title`, `content` 포함
- [ ] 게시글 수정 응답 DTO 추가
- [ ] 게시글 ID로 수정 대상 조회
- [ ] 사용자 ID 존재 여부 확인
- [ ] 카테고리 ID로 변경 대상 카테고리 조회
- [ ] 요청 사용자 ID와 게시글 작성자 ID 일치 여부 확인
- [ ] 작성자 불일치 시 403 응답 처리
- [ ] 게시글의 카테고리, 제목, 내용 수정
- [ ] 응답에 `postId`, `categoryId`, `title`, `content`, `updatedAt` 포함
- [ ] Validation / 예외 처리 추가
- [ ] 게시글 수정 테스트 추가 또는 기존 테스트 보강
- [ ] 새 API 확인용 `.http` 파일 추가/수정 및 가능한 범위에서 수동 확인
- [ ] `./gradlew test` 통과 확인

## 이번 PR에서 하지 않을 일

- 게시글 상세 조회
- 게시글 삭제
- 게시글 목록 조회 변경
- 검색 / 페이징 / 정렬 조건
- 댓글 기능
- 로그인/토큰 기반 인증 / 인가 구조 변경
- DB 구조 변경

## 완료 조건

- [ ] 기능 구현 완료
- [ ] Validation / 예외 처리 확인
- [ ] 테스트 통과
- [ ] 수동 API 확인 완료 또는 생략 사유 기록
- [ ] git diff 검토
- [ ] PR 본문 작성

## 예상 변경 파일

### 생성 가능

- `src/main/java/com/github/marcel615/askteacher/domain/post/dto/PostUpdateRequest.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/dto/PostUpdateResponse.java`

### 수정 가능

- `docs/requirements.md`
- `docs/api-spec.md`
- `docs/current-task.md`
- `src/main/java/com/github/marcel615/askteacher/domain/post/controller/PostController.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/service/PostService.java`
- 게시글 수정 관련 테스트 파일
- `src/main/java/com/github/marcel615/askteacher/http` 하위 `.http` 파일
