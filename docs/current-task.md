# Current Task

## Issue

- Issue: #25
- Title: [Feature] post: 게시글 파일 업로드 기능
- URL: https://github.com/marcel615/Ask-Teacher/issues/25
- State: OPEN

## 목표

게시글 작성 및 수정 시 이미지/PDF 파일을 첨부할 수 있도록 하고, 게시글 상세 조회 응답에서 첨부파일 정보를 확인할 수 있게 한다.

## 범위

- `POST /api/posts`에서 `multipart/form-data` 요청을 받아 게시글 내용과 파일을 함께 처리한다.
- `PATCH /api/posts/{postId}`에서 `multipart/form-data` 요청을 받아 게시글 수정 내용과 파일을 함께 처리한다.
- 첨부 가능한 파일 형식은 아래로 제한한다.
  - `image/jpeg`
  - `image/png`
  - `image/webp`
  - `application/pdf`
- 빈 파일 업로드를 거부한다.
- 파일 크기 제한을 적용한다.
- 첨부파일을 서버 저장소 또는 지정된 파일 저장 경로에 저장한다.
- 게시글 상세 조회 응답에 첨부파일 정보를 포함한다.
  - 저장 파일명
  - 파일 타입
  - 파일 URL 또는 파일 경로

## 제외 범위

- 프론트엔드 구현
- 첨부파일 삭제 API
- 기존 첨부파일 교체/삭제 정책 고도화
- 이미지 리사이징, 썸네일 생성, 바이러스 검사
- 외부 스토리지 연동
- 테스트 코드 작성은 이번 Issue 요구사항 기준으로 제외하고, `.http` 파일을 통한 수동 확인 범위까지만 진행한다.

## 요구사항 변경 요약

- `docs/requirements.md` 변경 필요
  - 게시글 첨부파일 기능 요구사항 추가
  - 기존 제외 범위의 파일 업로드 문구 조정

## API 변경 요약

- `POST /api/posts`
  - 기존 JSON 요청에서 `multipart/form-data` 요청 지원으로 변경
  - 게시글 필드와 `files`를 함께 수신
- `PATCH /api/posts/{postId}`
  - 기존 JSON 요청에서 `multipart/form-data` 요청 지원으로 변경
  - 게시글 수정 필드와 `files`를 함께 수신
- `GET /api/posts/{postId}`
  - 응답 `data.files`에 첨부파일 목록 추가

## ERD 변경

- ERD 변경 필요
- 사유: 하나의 게시글에 여러 첨부파일을 연결해야 하므로 `post_files` 테이블과 `Post 1 : N PostFile` 관계가 필요하다.
- 추가 Entity: `PostFile`
- 추가 테이블: `post_files`
- 주요 컬럼:
  - `post_id`
  - `original_file_name`
  - `stored_file_name`
  - `file_path`
  - `content_type`
  - `file_size`
  - `created_at`

## 예외 처리

- 인증되지 않은 사용자: 401 Unauthorized
- 허용되지 않은 파일 형식: 400 Bad Request
- 빈 파일 업로드: 400 Bad Request
- 파일 크기 초과: 400 Bad Request
- 파일 저장 실패: 500 Internal Server Error

## 수동 확인

- `.http` 파일로 가능한 범위까지 확인한다.
- 확인 대상:
  - 게시글 작성 multipart 요청
  - 게시글 수정 multipart 요청
  - 게시글 상세 조회 첨부파일 응답
  - 허용되지 않은 파일 형식
  - 빈 파일
  - 파일 크기 초과

## 예상 변경 파일

### Architect 사전 반영 문서

- `docs/current-task.md`
- `docs/requirements.md`
- `docs/api-spec.md`
- `docs/erd.md`

### Builder 구현 변경 예상 파일

- 게시글 요청 DTO 또는 multipart request DTO
- 게시글 응답 DTO
- 게시글 Controller
- 게시글 Service
- 게시글 Repository 또는 조회 로직
- `PostFile` Entity
- `PostFileRepository`
- 파일 저장 서비스 또는 파일 스토리지 컴포넌트
- 파일 업로드 설정 프로퍼티
- 예외 타입 또는 예외 처리 매핑
- DB 마이그레이션/DDL 관련 파일이 존재하는 경우 해당 파일
- `src/main/java/com/github/marcel615/askteacher/http` 하위 `.http` 파일

## 완료 조건

- 게시글 작성 시 허용된 이미지/PDF 파일을 함께 업로드할 수 있다.
- 게시글 수정 시 허용된 이미지/PDF 파일을 함께 업로드할 수 있다.
- 허용되지 않은 파일 형식, 빈 파일, 파일 크기 초과는 400으로 실패한다.
- 파일 저장 실패는 500으로 실패한다.
- 게시글 상세 조회 응답에 첨부파일 정보가 포함된다.
- `.http` 파일로 수동 확인 가능한 요청이 준비된다.
