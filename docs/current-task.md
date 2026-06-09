# Current Task

## Related Issue

- Issue number: N/A
- Issue title: refactor: convert DTO classes to Java records
- Issue URL: N/A

## Task

- Convert domain DTO classes to Java records.

## Scope

- Convert DTO classes under `src/main/java/com/github/marcel615/askteacher/domain/**/dto`.
- Keep existing validation annotations on request DTO components.
- Keep existing response JSON field names and factory methods.
- Update service and test code that calls DTO getters.
- Add `categoryName` to `PostListResponse`.
- Make public read APIs accessible without authentication.
- Keep post write APIs protected by authentication.
- Remove `userId` from authenticated post create/update request DTOs.
- Use authenticated JWT user id for post create/update/delete author checks.
- Use `Long` authentication principal for JWT user id.
- Normalize Korean text in `docs/api-spec.md` and `docs/requirements.md`.

## Out Of Scope

- Entity changes
- DB schema changes
- Authentication or authorization behavior changes outside post write API authentication
- API field additions or removals except `PostListResponse.categoryName` and authenticated post request `userId` removal
- Global response wrapper refactoring

## Branch

- Base branch: `develop`
- Working branch: `feature/dto-record-refactor`
- PR direction: `feature/dto-record-refactor` -> `develop`

## Completion Criteria

- [x] Domain DTO classes are records.
- [x] Validation annotations remain on request DTO components.
- [x] Existing factory methods still work.
- [x] Code compiles.
- [x] `./gradlew test` passes.
- [x] git diff reviewed.
- [x] `PostListResponse` includes category name.
- [x] Public read APIs do not require authentication.
- [x] Post write APIs require authentication.
- [x] Authenticated post create/update request DTOs do not accept `userId`.
- [x] Post create/update/delete use authenticated user id.
- [x] JWT authentication principal is `Long`.
- [x] API and requirements documents use readable Korean text.

## Expected Files

- `src/main/java/com/github/marcel615/askteacher/domain/auth/dto/*.java`
- `src/main/java/com/github/marcel615/askteacher/domain/category/dto/*.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/dto/*.java`
- `src/main/java/com/github/marcel615/askteacher/global/config/SecurityConfig.java`
- `src/main/java/com/github/marcel615/askteacher/global/security/jwt/JwtTokenProvider.java`
- `docs/api-spec.md`
- `docs/requirements.md`
- Service and test files that reference DTO getters
