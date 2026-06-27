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

## 인증

로그인 성공 시 발급받은 Access Token을 인증이 필요한 API 요청에 사용한다.

```http
Authorization: Bearer {accessToken}
```

Access Token의 subject에는 사용자 ID가 들어가며, 서버는 인증된 사용자 ID를 기준으로 게시글 작성자 검증을 수행한다. Refresh Token, 로그아웃, 토큰 재발급은 현재 범위에서 다루지 않는다.

## API 목록

| 기능 | Method | URL | 인증 | 상태 |
|---|---|---|---|---|
| 회원가입 | POST | `/api/auth/signup` | 불필요 | 구현됨 |
| 로그인 | POST | `/api/auth/login` | 불필요 | 구현됨 |
| 카테고리 목록 조회 | GET | `/api/categories` | 불필요 | 구현됨 |
| 게시글 작성 | POST | `/api/posts` | 필요 | 구현됨, multipart/form-data 첨부파일 지원 예정 |
| 게시글 목록 조회 | GET | `/api/posts` | 불필요 | 구현됨 |
| 게시글 상세 조회 | GET | `/api/posts/{postId}` | 불필요 | 구현됨, 첨부파일 정보 반환 예정 |
| 게시글 수정 | PATCH | `/api/posts/{postId}` | 필요 | 구현됨, multipart/form-data 첨부파일 지원 예정 |
| 게시글 삭제 | DELETE | `/api/posts/{postId}` | 필요 | 구현됨 |
| 게시글 좋아요 등록 | POST | `/api/posts/{postId}/likes` | 필요 | 구현 예정 |
| 게시글 좋아요 취소 | DELETE | `/api/posts/{postId}/likes` | 필요 | 구현 예정 |

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
    "accessToken": "eyJ...",
    "tokenType": "Bearer"
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
  "message": "카테고리 목록 조회에 성공했습니다.",
  "data": [
    {
      "id": 1,
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

```http
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data
```

| Part | 타입 | 필수 | 설명 |
|---|---|---|---|
| categoryId | Long | 필수 | 카테고리 ID |
| title | String | 필수 | 게시글 제목 |
| content | String | 필수 | 게시글 내용 |
| files | MultipartFile[] | 선택 | 첨부파일 목록 |

### Validation

| 필드 | 규칙 |
|---|---|
| categoryId | 필수, 존재하는 카테고리 ID |
| title | 필수, 100자 이하 |
| content | 필수, 5000자 이하 |
| files | 선택, 여러 파일 첨부 가능 |
| files.contentType | `image/jpeg`, `image/png`, `image/webp`, `application/pdf`만 허용 |
| files.size | 0보다 커야 하며 설정된 최대 크기 이하여야 함 |

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
| 인증 실패 | 401 Unauthorized |
| Validation 실패 | 400 Bad Request |
| 허용되지 않은 파일 형식 | 400 Bad Request |
| 빈 파일 | 400 Bad Request |
| 파일 크기 초과 | 400 Bad Request |
| 사용자 없음 | 404 Not Found |
| 카테고리 없음 | 404 Not Found |
| 파일 저장 실패 | 500 Internal Server Error |

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
      "categoryName": "Spring",
      "newPost": true,
      "likeCount": 3,
      "createdAt": "2026-05-12T19:30:00"
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
- 게시글 목록은 `createdAt` 기준 내림차순으로 정렬해 최근에 생성된 게시글부터 반환한다.

---

## 게시글 상세 조회

### Request

`GET /api/posts/{postId}`

### Validation

| 필드 | 규칙 |
|---|---|
| postId | 필수, 존재하는 게시글 ID |

### Response

```json
{
  "status": 200,
  "message": "게시글 상세 조회에 성공했습니다.",
  "data": {
    "postId": 1,
    "userName": "springUser",
    "categoryName": "Spring",
    "title": "Spring Bean은 무엇인가요?",
    "content": "Spring Bean 개념이 궁금합니다.",
    "likeCount": 3,
    "likedByMe": true,
    "createdAt": "2026-05-12T19:30:00",
    "files": [
      {
        "storedFileName": "uuid-image.png",
        "contentType": "image/png",
        "fileUrl": "/files/uuid-image.png"
      }
    ]
  }
}
```

### Status Code

| 상황 | Status |
|---|---|
| 조회 성공 | 200 OK |
| 게시글 없음 | 404 Not Found |

---

## 게시글 수정

### Request

`PATCH /api/posts/{postId}`

```http
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data
```

| Part | 타입 | 필수 | 설명 |
|---|---|---|---|
| categoryId | Long | 필수 | 카테고리 ID |
| title | String | 필수 | 게시글 제목 |
| content | String | 필수 | 게시글 내용 |
| files | MultipartFile[] | 선택 | 첨부파일 목록 |

### Validation

| 필드 | 규칙 |
|---|---|
| postId | 필수, 존재하는 게시글 ID |
| categoryId | 필수, 존재하는 카테고리 ID |
| title | 필수, 100자 이하 |
| content | 필수, 5000자 이하 |
| files | 선택, 여러 파일 첨부 가능 |
| files.contentType | `image/jpeg`, `image/png`, `image/webp`, `application/pdf`만 허용 |
| files.size | 0보다 커야 하며 설정된 최대 크기 이하여야 함 |

### Response

```json
{
  "status": 200,
  "message": "게시글이 수정되었습니다.",
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
| 인증 실패 | 401 Unauthorized |
| Validation 실패 | 400 Bad Request |
| 허용되지 않은 파일 형식 | 400 Bad Request |
| 빈 파일 | 400 Bad Request |
| 파일 크기 초과 | 400 Bad Request |
| 작성자 불일치 | 403 Forbidden |
| 게시글 없음 | 404 Not Found |
| 카테고리 없음 | 404 Not Found |
| 파일 저장 실패 | 500 Internal Server Error |

### 비고

- 작성자 검증은 Authorization 헤더의 Access Token 사용자 ID와 게시글 작성자 ID를 비교한다.
- 게시글 수정 시 `newPost`, `deleted`, `createdAt`, `user`는 변경하지 않는다.

---

## 게시글 삭제

### Request

`DELETE /api/posts/{postId}`

```http
Authorization: Bearer {accessToken}
```

### Validation

| 필드 | 규칙 |
|---|---|
| postId | 필수, 존재하는 게시글 ID, 삭제되지 않은 게시글 |

### Response

```json
{
  "status": 200,
  "message": "게시글이 삭제되었습니다."
}
```

### Status Code

| 상황 | Status |
|---|---|
| 삭제 성공 | 200 OK |
| 인증 실패 | 401 Unauthorized |
| 작성자 불일치 | 403 Forbidden |
| 게시글 없음 | 404 Not Found |

### 비고

- DB row를 물리 삭제하지 않고 `Post.deleted = true`로 변경한다.
- 이미 삭제된 게시글은 조회되지 않는 게시글로 보고 404 Not Found로 처리한다.
- 삭제 시 `updatedAt`을 갱신한다.
- 작성자 검증은 Authorization 헤더의 Access Token 사용자 ID와 게시글 작성자 ID를 비교한다.

---

## 게시글 좋아요 등록

### Request

`POST /api/posts/{postId}/likes`

```http
Authorization: Bearer {accessToken}
```

### Validation

| 필드 | 규칙 |
|---|---|
| postId | 필수, 존재하는 게시글 ID, 삭제되지 않은 게시글 |

### Response

```json
{
  "status": 200,
  "message": "게시글 좋아요가 등록되었습니다."
}
```

### Status Code

| 상황 | Status |
|---|---|
| 등록 성공 | 200 OK |
| 인증 실패 | 401 Unauthorized |
| 중복 좋아요 요청 | 400 Bad Request |
| 게시글 없음 | 404 Not Found |

---

## 게시글 좋아요 취소

### Request

`DELETE /api/posts/{postId}/likes`

```http
Authorization: Bearer {accessToken}
```

### Validation

| 필드 | 규칙 |
|---|---|
| postId | 필수, 존재하는 게시글 ID, 삭제되지 않은 게시글 |

### Response

```json
{
  "status": 200,
  "message": "게시글 좋아요가 취소되었습니다."
}
```

### Status Code

| 상황 | Status |
|---|---|
| 취소 성공 | 200 OK |
| 인증 실패 | 401 Unauthorized |
| 좋아요 취소 대상 없음 | 400 Bad Request |
| 게시글 없음 | 404 Not Found |
