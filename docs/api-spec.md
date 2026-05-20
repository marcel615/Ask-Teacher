# API 명세

## 공통 성공 응답

```json
{
  "status": 200,
  "message": "요청에 성공했습니다.",
  "data": {}
}
```

## 공통 에러 응답

```json
{
  "status": 400,
  "message": "에러 메시지"
}
```

## API 목록

| 기능 | Method | URL | 상태 |
|---|---|---|---|
| 회원가입 | POST | `/api/auth/signup` | 구현됨 |
| 로그인 | POST | `/api/auth/login` | 구현됨 |
| 카테고리 목록 조회 | GET | `/api/categories` | 구현됨 |
| 게시글 작성 | POST | `/api/posts` | 구현됨 |
| 게시글 목록 조회 | GET | `/api/posts` | 구현됨 |
| 게시글 수정 | PATCH | `/api/posts/{postId}` | 구현 예정 |

---

## 회원가입

### Request

`POST /api/auth/signup`

```json
{
  "email": "user@example.com",
  "password": "password123",
  "nickname": "springUser"
}
```

### Validation

| 필드 | 규칙 |
|---|---|
| email | 필수, 이메일 형식, 중복 불가 |
| password | 필수, 8자 이상 |
| nickname | 필수, 2자 이상 20자 이하, 중복 불가 |

### Response

```json
{
  "status": 201,
  "message": "회원가입 성공!",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "nickname": "springUser"
  }
}
```

### Status Code

| 상황 | Status |
|---|---|
| 생성 성공 | 201 Created |
| Validation 실패 | 400 Bad Request |
| 이메일 또는 닉네임 중복 | 409 Conflict |

---

## 로그인

### Request

`POST /api/auth/login`

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

### Validation

| 필드 | 규칙 |
|---|---|
| email | 필수, 이메일 형식 |
| password | 필수 |

### Response

```json
{
  "status": 200,
  "message": "로그인에 성공했습니다.",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "nickname": "springUser"
  }
}
```

### Status Code

| 상황 | Status |
|---|---|
| 성공 | 200 OK |
| Validation 실패 | 400 Bad Request |
| 로그인 정보 불일치 | 401 Unauthorized |

---

## 카테고리 목록 조회

### Request

`GET /api/categories`

### Response

```json
{
  "status": 200,
  "message": "카테고리 목록이 반환에 성공하였습니다.",
  "data": [
    {
      "categoryId": 1,
      "name": "Spring"
    }
  ]
}
```

### Status Code

| 상황 | Status |
|---|---|
| 성공 | 200 OK |

---

## 게시글 작성

### Request

`POST /api/posts`

```json
{
  "userId": 1,
  "categoryId": 1,
  "title": "Spring Bean이 뭔가요?",
  "content": "Spring Bean의 개념이 궁금합니다."
}
```

### Validation

| 필드 | 규칙 |
|---|---|
| userId | 필수, 존재하는 사용자 ID |
| categoryId | 필수, 존재하는 카테고리 ID |
| title | 필수, 100자 이하 |
| content | 필수, 5000자 이하 |

### Response

```json
{
  "status": 201,
  "message": "게시글이 작성되었습니다.",
  "data": {
    "postId": 1,
    "title": "Spring Bean이 뭔가요?",
    "newPost": true,
    "createdAt": "2026-05-12T19:30:00"
  }
}
```

### Status Code

| 상황 | Status |
|---|---|
| 생성 성공 | 201 Created |
| Validation 실패 | 400 Bad Request |
| 사용자 없음 | 404 Not Found |
| 카테고리 없음 | 404 Not Found |

---

## 게시글 목록 조회

### Request

`GET /api/posts`

### Response

```json
{
  "status": 200,
  "message": "게시글 목록 조회에 성공했습니다.",
  "data": [
    {
      "postId": 1,
      "title": "Spring Bean이 뭔가요?",
      "writerNickname": "springUser",
      "newPost": true,
      "createdAt": "2026-05-12T19:30:00"
    },
    {
      "postId": 2,
      "title": "DTO와 Entity 차이가 뭔가요?",
      "writerNickname": "backendUser",
      "newPost": false,
      "createdAt": "2026-05-11T14:20:00"
    }
  ]
}
```

### Status Code

| 상황 | Status |
|---|---|
| 성공 | 200 OK |

### 비고

- 삭제된 게시글은 목록에서 제외한다.
- Issue #5 범위에는 검색, 페이징, 정렬 조건을 포함하지 않는다.

---

## 게시글 수정

### Request

`PATCH /api/posts/{postId}`

```json
{
  "userId": 1,
  "categoryId": 1,
  "title": "수정 게시글 제목",
  "content": "수정 게시글 내용"
}
```

### Validation

| 필드 | 규칙 |
|---|---|
| postId | 필수, 존재하는 게시글 ID |
| userId | 필수, 존재하는 사용자 ID, 게시글 작성자 ID와 일치 |
| categoryId | 필수, 존재하는 카테고리 ID |
| title | 필수, 100자 이하 |
| content | 필수, 5000자 이하 |

### Response

```json
{
  "status": 200,
  "message": "게시글 수정에 성공했습니다.",
  "data": {
    "postId": 1,
    "categoryId": 1,
    "title": "수정 게시글 제목",
    "content": "수정 게시글 내용",
    "updatedAt": "2026-05-12T19:30:00"
  }
}
```

### Status Code

| 상황 | Status |
|---|---|
| 수정 성공 | 200 OK |
| Validation 실패 | 400 Bad Request |
| 작성자 불일치 | 403 Forbidden |
| 게시글 없음 | 404 Not Found |
| 사용자 없음 | 404 Not Found |
| 카테고리 없음 | 404 Not Found |

### 비고

- 이번 Issue 범위에는 로그인/토큰 기반 인증/인가 구조 변경을 포함하지 않는다.
- 작성자 검증은 request body의 `userId`와 게시글 작성자 ID를 비교하는 방식으로 처리한다.
- 게시글 수정 시 `newPost`, `deleted`, `createdAt`, `user`는 변경하지 않는다.
