# Current Task

## Issue

- Issue: #26
- Title: [Feature] post: 게시글 검색 / 페이징 / 카테고리 조회 기능
- URL: https://github.com/marcel615/Ask-Teacher/issues/26
- State: OPEN

## 목표

게시글 목록 조회 API에서 전체 데이터를 한 번에 반환하지 않고, 검색 조건, 카테고리 조건, 페이지 조건을 조합해 최신순으로 조회할 수 있도록 개선한다.

## 범위

- `GET /api/posts`에서 페이지 단위 조회를 지원한다.
- `GET /api/posts`에서 제목 또는 내용 기준 keyword 검색을 지원한다.
- `GET /api/posts`에서 categoryId 기준 카테고리 필터를 지원한다.
- keyword와 categoryId 조건을 함께 사용할 수 있다.
- keyword가 없거나 공백이면 검색 조건 없이 조회한다.
- categoryId가 없으면 전체 카테고리 게시글을 조회한다.
- 게시글 목록은 생성일시 기준 최신순으로 정렬한다.
- 삭제된 게시글은 목록에서 제외한다.
- 응답에는 게시글 목록과 페이지 정보를 포함한다.

## 제외 범위

- 프론트엔드 구현
- Entity 변경
- DB 테이블 변경
- 인증/인가 구조 변경
- 게시글 상세 조회 변경
- 게시글 작성/수정/삭제 변경
- 카테고리 CRUD
- 별도 정렬 조건 파라미터 추가
- 테스트 코드 작성

## 요구사항 변경 요약

- `docs/requirements.md` 변경 필요
  - 게시글 목록 조회에 검색, 페이징, 카테고리 필터 조건 추가
  - 기존 제외 범위의 검색/페이징/정렬 파라미터 제외 문구를 Issue #26 범위에 맞게 조정

## API 변경 요약

- `GET /api/posts`
  - query parameter 추가 또는 명세화
    - `keyword`: 선택, 제목 또는 내용 검색어
    - `categoryId`: 선택, 카테고리 ID
    - `page`: 선택, 0 이상, 기본값 0
    - `size`: 선택, 1 이상, 기본값 10
  - 요청 예시
    - `GET /api/posts?page=0&size=10`
    - `GET /api/posts?keyword=검색어&page=0&size=10`
    - `GET /api/posts?categoryId=1&page=0&size=10`
    - `GET /api/posts?keyword=검색어&categoryId=1&page=0&size=10`
  - 응답 data는 게시글 목록과 페이지 정보를 포함한다.
    - `content`
    - `currentPage`
    - `pageSize`
    - `totalElements`
    - `totalPages`
    - `hasNext`
    - `hasPrevious`
    - `isFirst`
    - `isLast`

## ERD 변경

- ERD 변경 없음
- 사유:
  - Entity 변경 없음
  - DB 테이블 변경 없음
  - 기존 `Post -> Category` 관계로 카테고리 필터 조회 가능
  - keyword 검색, 페이징, 최신순 정렬은 Repository 조회 조건 변경으로 처리

## Validation

- `page`는 0 이상이어야 한다.
- `size`는 1 이상이어야 한다.
- `keyword`는 선택값이다.
- `keyword`가 없거나 공백이면 검색 조건 없이 조회한다.
- `categoryId`는 선택값이다.
- `categoryId`가 없으면 전체 카테고리 게시글을 조회한다.
- 존재하지 않는 `categoryId`로 요청하면 404 Not Found로 처리한다.

## 예외 처리

- 잘못된 페이지 값: 400 Bad Request
- 잘못된 페이지 크기 값: 400 Bad Request
- 존재하지 않는 카테고리: 404 Not Found
- 서버 조회 실패: 500 Internal Server Error

## 수동 확인

- `.http` 파일로 가능한 범위까지 확인한다.
- 확인 대상:
  - 전체 게시글 페이징 조회
  - keyword 검색 + 페이징 조회
  - categoryId 필터 + 페이징 조회
  - keyword + categoryId 조합 조회
  - page가 0 미만인 요청
  - size가 1 미만인 요청
  - 존재하지 않는 categoryId 요청

## 예상 변경 파일

### Architect 사전 반영 문서

- `docs/current-task.md`
- `docs/requirements.md`
- `docs/api-spec.md`

### Builder 구현 변경 예상 파일

- 게시글 목록 조회 Request DTO 또는 query parameter 처리 코드
- 게시글 목록 조회 Response DTO
- 페이지 응답 DTO
- 게시글 Controller
- 게시글 Service
- 게시글 Repository 또는 검색/페이징 조회 로직
- 카테고리 존재 여부 확인 로직
- 예외 타입 또는 예외 처리 매핑
- `src/main/java/com/github/marcel615/askteacher/http` 하위 `.http` 파일

## 완료 조건

- `GET /api/posts?page=0&size=10` 요청으로 전체 게시글을 페이지 단위로 조회할 수 있다.
- `GET /api/posts?keyword=검색어&page=0&size=10` 요청으로 제목 또는 내용 검색 결과를 페이지 단위로 조회할 수 있다.
- `GET /api/posts?categoryId=1&page=0&size=10` 요청으로 특정 카테고리 게시글을 페이지 단위로 조회할 수 있다.
- `GET /api/posts?keyword=검색어&categoryId=1&page=0&size=10` 요청으로 검색어와 카테고리 조건을 함께 적용할 수 있다.
- 응답에 게시글 목록과 페이지 정보가 포함된다.
- 목록은 최신순으로 정렬된다.
- 잘못된 page/size 요청은 400으로 실패한다.
- 존재하지 않는 categoryId 요청은 404로 실패한다.
- `.http` 파일로 수동 확인 가능한 요청이 준비된다.
