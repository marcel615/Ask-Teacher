# Devlog

## 2026-05-20

### Issue / PR

- Issue: #5 feat(post): 게시글 목록 조회
- Branch: `feature/issue-5-post-list`
- PR: #6 feat(post): 게시글 목록 조회 API 구현
- PR URL: https://github.com/marcel615/Ask-Teacher/pull/6

### 오늘의 작업

- Issue #5 내용을 기준으로 게시글 목록 조회 요구사항을 정리했다.
- `requirements.md`, `api-spec.md`, `erd.md`를 실제 프로젝트 구조 기준으로 초기 작성했다.
- `docs/current-task.md`에 Issue #5 구현 범위를 정리했다.
- `GET /api/posts` 게시글 목록 조회 API 구현 PR을 리뷰했다.
- Architect 리뷰 코멘트를 PR #6에 남겼다.
- 리뷰 결과에 따라 `docs/api-spec.md`의 게시글 목록 조회 상태를 `구현됨`으로 갱신했다.
- 게시글 목록 응답 필드명을 `newPost` 기준으로 정리했다.

### 변경 내용

- 게시글 목록 조회 API 명세 추가
  - `GET /api/posts`
  - 응답 필드: `postId`, `title`, `writerNickname`, `newPost`, `createdAt`
- 게시글 목록 조회 구현 범위 문서화
  - 삭제된 게시글 제외
  - 검색 / 페이징 / 정렬 / 상세 조회는 제외
- 실제 코드 구조 기준 ERD 정리
  - `User`
  - `Category`
  - `Post`
- API 공통 응답 형식 정리
  - 성공 응답: `status/message/data`
  - 에러 응답: `status/message`

### 테스트 결과

```bash
./gradlew test
```

- 결과: 통과
- 확인 내용:
  - 최신 PR 상태에서 `BUILD SUCCESSFUL`
  - 게시글 목록 조회 서비스 테스트 통과

### 문제 / 해결

- 문제:
  - 초기 문서의 `Member` 엔티티와 기존 공통 응답 형식이 실제 코드와 맞지 않았다.
- 해결:
  - 실제 코드 기준의 `User`, `Category`, `Post` 엔티티와 `status/message/data` 응답 형식으로 문서를 재정리했다.

- 문제:
  - PR 범위가 Issue #5 구현 범위보다 넓게 보였다.
- 해결:
  - PR 본문에 Issue 기반 개발 흐름 문서와 템플릿을 함께 포함한다는 의도를 명시했다.

- 문제:
  - 게시글 목록 조회 상태가 `TODO`로 남아 있었다.
- 해결:
  - `docs/api-spec.md`에서 `구현됨`으로 갱신했다.

- 문제:
  - 게시글 목록 응답 필드명이 `isNew`와 `newPost` 사이에서 불일치할 수 있었다.
- 해결:
  - 프로젝트 엔티티 필드명과 맞춰 문서 기준을 `newPost`로 정리했다.

### AI 활용 기록

- Architect:
  - Issue #5 분석
  - 요구사항/API/ERD/current-task 문서 정리
  - PR #6 리뷰
  - PR 코멘트 작성
  - 리뷰 반영 여부 재검토
  - 이번 Issue/PR devlog 초안 작성
- Builder:
  - `GET /api/posts` 구현
  - `PostListResponse` 추가
  - 삭제 게시글 제외 조회 구현
  - 서비스 테스트 추가
- ChatGPT:
  - 문서 구조화, 리뷰 항목 분류, 반영 여부 검토 보조

### 다음 작업 후보

- 게시글 목록 응답 필드명이 코드에서도 `newPost`로 완전히 반영되었는지 최종 확인
- Controller/WebMvc 테스트를 추가해 실제 JSON 응답 필드명 검증
- PR 범위가 큰 문서/템플릿 추가 작업을 별도 PR로 분리할지 결정
- Issue #5 병합 후 다음 게시글 기능 Issue 선정
