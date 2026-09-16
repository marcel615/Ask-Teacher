# Current Work Log

## Issue #33 — 전체 API 자동 테스트 구축 및 수동 테스트 지침 전환

- 승인된 Architect 사전 문서인 docs/current-task.md 변경을 보존했다. 원격 fetch 후 develop과 origin/develop의 차이가 0/0임을 확인하고 feature/issue-33-api-automated-tests를 생성했다.
- 최초 fetch는 sandbox의 .git 쓰기 제한으로 실패했으며, 승인된 권한으로 재실행하여 성공했다.
- 기존 Issue #32 임시 기록을 현재 Issue 기준으로 초기화했다.

## 주요 결정 및 중간 검증

- 기존 PostServiceTest의 13개 DB 기반 검증을 PostServiceIntegrationTest로 이동해 보존하고, PostServiceTest는 mock 기반 단위 테스트로 분리했다.
- Spring 컨텍스트 테스트에 test 프로필을 적용했다. Repository는 H2 JPA 슬라이스, API는 실제 Security/Service/Repository를 연결한 MockMvc, 업로드 제한은 RANDOM_PORT 실제 HTTP로 검증한다.
- API 테스트는 매 테스트 전후 DB 데이터를 외래키 역순으로 지우고 JUnit 임시 디렉터리 파일을 정리한다. 실제 HTTP 요청의 저장 데이터는 테스트 스레드 롤백에 의존하지 않는다.
- 최초 Gradle 실행은 sandbox 네트워크 제한으로 실패했다. 허용된 권한으로 실행한 기존 검증은 통과했다.
- 계층 테스트 첫 실행: 84개 중 6개 실패. 새 매개변수 테스트의 배열 인자 전달 오류였으며 메서드 인자를 개별 필드로 수정했다.
- 다음 전체 실행: 93개 중 91개 통과, 2개 실패. 실패는 인증 토큰 누락/잘못된 토큰의 명세 401 대비 실제 403 불일치다. 기대값을 완화하거나 비활성화하지 않고 처리 방향을 사용자에게 문의했다. 5개 보호 API 각각에 대해 독립된 시나리오로 확장한다.
- 실제 HTTP 10MB 경계 업로드 성공 및 10MB 초과 POST/PATCH 요청 400 검증은 통과했다.
- 삭제된 게시글 수정은 findById를 사용하므로 삭제 여부를 확인하지 않는다. 수정 명세는 삭제된 대상의 정책을 명시하지 않으며 deleted 유지만 규정한다. 정책 확인 후보로 기록하고 운영 코드는 변경하지 않는다.
- H2 테스트는 MySQL 고유 동작 및 성능·동시성 부하를 보장하지 않는다.

## API별 테스트 대응표

Java 경로 기준: `src/test/java/com/github/marcel615/askteacher/`.
Controller·Service·Repository 클래스는 각 `domain/{도메인}/{계층}/` 아래에 있다.

| API | 시나리오 및 계층 테스트 | 통합 테스트 메서드 (`integration/ApiIntegrationTest`) |
|---|---|---|
| POST /api/auth/signup | `AuthControllerTest.signupAndLoginBindJsonAndReturnDirectDtos`, `signupRejectsInvalidInput`, `nicknameMaximumLengthIsAccepted`; `AuthServiceTest.signupEncodesPasswordAndMapsSavedUser`, `duplicateEmailStopsBeforeSaving`, `duplicateNicknameStopsBeforeSaving`; `UserRepositoryTest` 이메일·닉네임 유니크 및 매핑 | `signupLoginEncryptsPasswordAndRejectsDuplicatesAndBadCredentials` |
| POST /api/auth/login | `AuthControllerTest.signupAndLoginBindJsonAndReturnDirectDtos`, `loginRejectsInvalidInput`, `businessErrorsKeepStatusAndErrorBody`; `AuthServiceTest.loginIssuesTokenForMatchedUser`, `missingEmailAndWrongPasswordRejectLogin`; `UserRepositoryTest.persistsAndFindsUserByEmailAndNickname` | `signupLoginEncryptsPasswordAndRejectsDuplicatesAndBadCredentials`, `postLifecycleWithFilesLikesAndSoftDelete`에서 실제 발급 토큰 사용 |
| GET /api/categories | `CategoryControllerTest.publicListReturnsArrayAndEmptyArray`; `CategoryServiceTest.mapsCategoriesAndHandlesEmptyList`; `CategoryRepositoryTest.savesAndFindsCategory`, `nameMustBeUnique` | `publicCategoriesMapDatabaseAndHandleEmptyList` |
| POST /api/posts | `PostControllerTest.multipartCreateAndPatchBindFieldsFilesAndPrincipal`, `noAttachmentAndMaximumFieldLengthsAreAccepted`, `createAndUpdateRejectInvalidFields`; `PostServiceTest.createSavesInitialStateAndAttachments`, `createRejectsMissingUserAndCategory`; PostRepository/PostFileRepository 저장·매핑 | `postLifecycleWithFilesLikesAndSoftDelete`, `missingResourcesWrongAuthorAndInvalidInputDoNotModifyPost`, `invalidFilesRollbackCreateAndUpdate`, `realStorageFailureReturns500AndRollsBackDatabase` |
| GET /api/posts | `PostControllerTest.publicListMapsDefaultsFiltersAndPageDto`; `PostServiceTest.searchNormalizesKeywordAndBuildsPage`, `blankSearchIsUnfiltered`, `invalidPagingAndCategoryStopSearch`; `PostRepositoryTest.searchCombinesTitleContentCategoryPagingSortAndDeletedFilter` | `searchPagingCategoryCombinationsAndBlankKeyword`, `postLifecycleWithFilesLikesAndSoftDelete` |
| GET /api/posts/{postId} | `PostControllerTest.detailPassesOptionalPrincipalAndDeleteReturnsNoBody`; `PostServiceTest.detailMapsFilesAndCurrentUsersLike`, `missingDetailIsNotFound`; `PostFileRepositoryTest.filesAreScopedToPostOrderedAndMetadataIsPersisted` | `postLifecycleWithFilesLikesAndSoftDelete`, `missingResourcesWrongAuthorAndInvalidInputDoNotModifyPost` |
| PATCH /api/posts/{postId} | `PostControllerTest` multipart·Validation·경계값; `PostServiceTest.updateChangesEditableFieldsAndPreservesOthers`, `updateRejectsMissingPostWrongAuthorAndMissingCategory`, `storageFailurePropagates`; `PostRepositoryTest.dirtyCheckingPersistsUpdateAndSoftDelete` | `postLifecycleWithFilesLikesAndSoftDelete` 첨부 추가/유지, `missingResourcesWrongAuthorAndInvalidInputDoNotModifyPost`, `invalidFilesRollbackCreateAndUpdate`, `realStorageFailureReturns500AndRollsBackDatabase` |
| DELETE /api/posts/{postId} | `PostControllerTest.detailPassesOptionalPrincipalAndDeleteReturnsNoBody`, `businessFailuresKeepExistingErrorContract`; `PostServiceTest.deleteChecksAuthorAndSoftDeletes`; `PostRepositoryTest.dirtyCheckingPersistsUpdateAndSoftDelete` | `postLifecycleWithFilesLikesAndSoftDelete` 삭제·재삭제·조회 제외, `missingResourcesWrongAuthorAndInvalidInputDoNotModifyPost` |
| POST /api/posts/{postId}/likes | `PostLikeControllerTest.passesAuthenticatedUserAndReturnsEmptyBodies`, `mapsBusinessErrorsAndInvalidPath`; `PostLikeServiceTest.likeSavesRelationBeforeIncreasingCount`, `duplicateLikeAndConstraintRaceDoNotIncreaseCount`, `missingOrDeletedPostAndMissingUserRejectBothActions`; `PostLikeRepositoryTest` 관계 조회·유니크, `PostRepositoryTest.bulkLikeUpdatesAreVisibleAfterReloadAndNeverGoBelowZero` | `postLifecycleWithFilesLikesAndSoftDelete` 등록·중복·목록/상세/DB 반영, `missingResourcesWrongAuthorAndInvalidInputDoNotModifyPost` |
| DELETE /api/posts/{postId}/likes | `PostLikeControllerTest` 상태·빈 본문·오류; `PostLikeServiceTest.unlikeDeletesRelationAndDecreasesCount`, `missingLikeDoesNotDecreaseCount`, `missingOrDeletedPostAndMissingUserRejectBothActions`; PostLikeRepository 삭제 후 재조회·PostRepository 감소 | `postLifecycleWithFilesLikesAndSoftDelete` 취소·재취소·DB/응답 반영, `missingResourcesWrongAuthorAndInvalidInputDoNotModifyPost` |

공통 검증:
- 보호 API 5개 × 토큰 누락/잘못된 토큰: `ApiIntegrationTest.protectedApisRejectMissingOrInvalidTokenAccordingToSpecification` 10개 독립 실행. 명세 401 대비 실제 403으로 실패하며 활성 상태를 유지한다.
- 파일 저장: `PostFileStorageTest` 허용 MIME 4종, 빈 파일, 크기 경계/초과, 실제 바이트·메타데이터, IOException/IllegalStateException 매핑.
- 서블릿 제한: `UploadLimitIntegrationTest.realServletAcceptsExactLimitAndRejectsOversizedCreateAndPatch` 실제 HTTP 10MB 성공, 10MB+1 바이트 POST/PATCH 거부 및 DB 저장 건수 확인.
- 기존 검증 유지: `PostServiceIntegrationTest` 13개, `SecurityConfigTest`, `AskteacherApplicationTests`; JWT 검증은 기존 principal 검증에 토큰 유효성·만료·잘못된 서명을 추가했다.

## 최종 검증 및 남은 사항

- `.\gradlew.bat test`: 104개 실행, 94개 통과, 10개 실패, skip 0. 실패는 위 인증 실패 응답 불일치뿐이다.
- `.\gradlew.bat test --rerun-tasks`: 104개 재실행, 94개 통과, 같은 10개 실패. 데이터·파일 잔존으로 인한 추가 실패는 나타나지 않았다. 전체 통과 완료 조건은 미충족이다.
- 기존 .http 9개에 있던 시나리오를 자동 테스트로 옮긴 후 해당 파일들을 삭제했다. 신규/변경 API는 없고 수동 API 확인은 실행하지 않았다. 실제 HTTP 확인은 자동 서버 테스트로 수행했다.
- 작업 지침 검색에서 README.md의 기술 스택·수동 API 확인 절에도 현재 .http 사용 안내가 남아 있어, current-task.md의 추가 지침 정비 범위에 따라 해당 부분만 자동 테스트 안내로 변경했다. README의 다른 오래된 기능 설명과 과거 devlog는 수정하지 않았다.
- 운영 Java 코드, 운영 설정, DB 구조, 인증·인가 구조, build.gradle은 변경하지 않았다.
- 인증 응답 불일치 처리 방향과 Issue #33 완료 여부는 사용자 협의 대기다. 별도 Issue 생성, git add/commit/push, PR 생성은 수행하지 않았다.
- 최종 git diff --check 통과. 현재 적용 지침(AGENTS, README, prompts, review-checklist, skill, Issue/PR 템플릿)에서 .http 작성·수동 API 확인 의무가 남아 있지 않음을 검색 확인했다.
- 변경 파일 총 42개: 기존 수정 13개(Architect 사전 문서 포함), 삭제 9개, 신규 20개. 신규 파일은 테스트 클래스 16개, 공통 테스트 지원 3개, 테스트 설정 1개다.

## 승인 범위 확장 — 미인증 응답 수정

- Architect가 수정한 current-task.md와 사용자의 계속 진행 지시에 따라 미인증 401 JSON 응답 수정을 진행했다. 동일 Issue이므로 기존 기록은 유지했다.
- 운영 변경은 SecurityConfig의 AuthenticationEntryPoint와 ErrorCode.UNAUTHORIZED 추가에 한정했다. 애플리케이션 ObjectMapper로 기존 ErrorResponse를 직렬화하고 application/json 및 UTF-8을 지정한다.
- 보호 API 5개 × 토큰 누락/무효 10개 검증에 401, Content-Type, UTF-8, 합의된 JSON 본문 검증을 추가했다. 비작성자 수정·삭제 403 오류 본문 및 로그인 실패 응답 유지 검증을 강화했다.
- SecurityConfigTest의 isIn(401, 403)을 제거하고 실제 HTTP 토큰 누락/무효 각각에서 정확한 401 및 JSON 본문·Content-Type·UTF-8을 검증하도록 변경했다.
- 앞선 인증 불일치 처리 협의 대기 상태는 이번 승인으로 해소됐으며, 아래 후속 검증 결과가 이전 미완료 보고를 대체한다.
- 후속 전체 검증: .\gradlew.bat test — 105개 실행, 실패 0, skip 0, BUILD SUCCESSFUL.
- 실제 반복 검증: .\gradlew.bat test --rerun-tasks — 105개 실행, 실패 0, skip 0, BUILD SUCCESSFUL. 기존 104개 시나리오를 유지하고 실제 HTTP 무효 토큰 검증 1개를 추가했다.
- 이전 10개 인증 응답 불일치가 모두 해결됐다. 공개 조회·로그인 실패·비작성자 403 및 실제 HTTP 업로드 검증도 통과했다.
- git diff --check 통과, isIn(401, 403) 잔존 없음. 운영 Java 변경은 승인된 두 파일뿐이다. JWT 필터·발급·검증·접근 허용 규칙은 유지했다.
- 수동 .http 확인은 이번 승인안에 따라 수행하지 않았다. 실제 HTTP 인증 응답은 RANDOM_PORT 자동 테스트로 검증했다.
- 변경 총 44개 파일(기존 수정 15, 삭제 9, 신규 20). Architect 문서는 그대로 유지했으며 git add/commit/push 및 PR 생성은 하지 않았다.
- 남은 범위 밖 참고 사항: 삭제된 게시글 수정 정책은 기존 기록대로 후속 정책 검토 후보이며 이번 변경 대상이 아니다. H2 검증의 MySQL 고유 동작 한계도 유지한다.
- 사용자 요청으로 PostServiceIntegrationTest와 SecurityConfigTest의 ActiveProfiles를 import + 짧은 어노테이션 표기로 통일했다. AskteacherApplicationTests에는 두 표기가 중복되어 있어 기존 import와 @ActiveProfiles("test")만 유지했다.
- 표기 정리 후 .\gradlew.bat test 재실행 BUILD SUCCESSFUL, git diff --check 통과. 테스트 동작 변경은 없다.
