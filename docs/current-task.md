# Current Task

## Issue

- Issue: #32
- Title: refactor: API 공통 응답 타입 개선
- URL: https://github.com/marcel615/Ask-Teacher/issues/32
- State: OPEN

## 목표

모든 기존 성공 API 응답에서 `ApiResponse<T>` 래퍼를 제거하고,
`ResponseEntity`의 본문에 기존 응답 DTO 또는 목록을 직접 반환한다.

## 범위

- 인증, 게시글, 카테고리, 게시글 좋아요 API의 성공 응답 반환 타입을 `ResponseEntity`로 변경한다.
- 데이터가 있는 응답은 기존 DTO 또는 목록을 본문으로 직접 반환한다.
- 데이터가 없는 게시글 삭제·좋아요 등록·취소 응답은 본문 없이 반환한다.
- 게시글 삭제와 좋아요 취소는 204 No Content로 변경하고, 나머지 API의 기존 성공 HTTP 상태 코드는 유지한다.
- 더 이상 사용하지 않는 `ApiResponse<T>` 성공 응답 타입을 정리한다.
- `docs/api-spec.md`의 공통 및 개별 API 성공 응답 명세를 변경된 응답 형식에 맞게 갱신한다.

## 제외 범위

- 테스트 파일 작성, 테스트 실행, `.http` 수동 확인
- 오류 응답 구조 또는 `GlobalExceptionHandler` 처리 변경
- API Method, URL, Request 형식 변경
- DB 구조, Entity, 인증·인가 동작 변경
- 이번 응답 변경과 무관한 리팩터링

## 요구사항 변경 요약

- `docs/requirements.md` 변경 없음
- 사유: 기존 사용자 기능 요구사항은 유지되고, 공통 응답 래퍼 규칙은 이 문서에 없음

## API 변경 요약

- 모든 기존 성공 API에서 응답 본문의 `status`, `message`, `data` 래퍼를 제거한다.
- 회원가입·게시글 작성: 201 Created, 기존 DTO를 본문으로 반환한다.
- 로그인·카테고리 조회·게시글 조회·수정: 200 OK, 기존 DTO 또는 목록을 본문으로 반환한다.
- 게시글 삭제·좋아요 취소: 204 No Content, 본문 없이 반환한다.
- 게시글 좋아요 등록: 200 OK, 본문 없이 반환한다.
- 기존 Method, URL, Request 및 오류 응답은 유지한다.
- `docs/api-spec.md`의 공통 성공 응답과 각 대상 API의 Response 예시를 갱신한다.

## ERD 변경

- ERD 변경 없음
- 사유: 응답 본문의 래퍼만 바꾸며 Entity, DB 테이블, 관계, 제약조건은 변경하지 않음

## Validation

- 기존 Request DTO 검증 규칙 유지
- 새 검증 규칙 추가 없음

## 예외 처리

- 기존 실패 조건, 실패 HTTP 상태 코드, `GlobalExceptionHandler`의 오류 응답 유지

## 예상 변경 파일

### Architect 사전 반영 문서

- `docs/current-task.md`
- `docs/api-spec.md`

### Builder 구현 변경 예상 파일

- `src/main/java/com/github/marcel615/askteacher/domain/auth/controller/AuthController.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/controller/PostController.java`
- `src/main/java/com/github/marcel615/askteacher/domain/category/controller/CategoryController.java`
- `src/main/java/com/github/marcel615/askteacher/domain/postlike/controller/PostLikeController.java`
- `src/main/java/com/github/marcel615/askteacher/global/response/ApiResponse.java` — 미사용 타입 정리; 삭제가 필요하면 별도 승인

## 완료 조건

- [ ] 모든 대상 API의 성공 반환 타입이 `ResponseEntity`다.
- [ ] 데이터가 있는 성공 응답은 기존 DTO 또는 목록을 본문으로 직접 반환한다.
- [ ] 데이터가 없는 성공 응답은 본문이 없다.
- [ ] 게시글 삭제·좋아요 취소는 204, 나머지 API의 기존 성공 HTTP 상태 코드와 오류 응답 처리는 유지된다.
- [ ] 미사용 `ApiResponse<T>` 성공 응답 타입이 정리된다.
- [ ] `docs/api-spec.md`의 성공 응답 명세가 변경된 응답 형식과 일치한다.
