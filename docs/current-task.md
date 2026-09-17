# Current Task

## Issue

- Issue: #39
- Title: [Refactor] BaseEntity 도입 및 JPA Auditing 적용
- URL: https://github.com/marcel615/Ask-Teacher/issues/39
- State: OPEN
- Labels: 없음

## 목표

각 Entity가 개별적으로 관리하는 `id`, `createdAt`, `updatedAt` 필드를
공통 기반 Entity로 통합한다.

JPA Auditing을 적용하여 Entity의 생성·수정 시각을 자동으로 관리하고,
팩토리·수정·삭제 메서드에서 `LocalDateTime.now()`를 직접 할당하는
중복 코드를 제거한다.

기존 API 동작과 DB 테이블·컬럼 매핑은 유지한다.

## 범위

- `BaseEntity` 구현
- `BaseUpdatableEntity` 구현
- JPA Auditing 활성화
- 전체 기존 Entity에 적절한 기반 클래스 적용
- Entity에 중복 선언된 `id`, `createdAt`, `updatedAt` 제거
- Entity 내부의 수동 생성·수정 시각 할당 제거
- 기존 ID 타입과 생성 전략 유지
- 기존 테이블 및 컬럼 매핑 유지
- 생성 시각과 수정 시각의 자동 설정 검증
- 기존 전체 테스트 회귀 검증
- 테스트 범위, 실행 결과 및 미검증 사항 기록

## 제외 범위

- API 요청·응답 구조 변경
- DB 테이블 또는 컬럼 구조 변경
- DB 마이그레이션
- 인증·인가 변경
- `createdBy`, `updatedBy` 등 사용자 기반 감사 기능
- 삭제 시각 또는 soft delete 공통화
- 시간 타입 및 시간대 정책 변경
- Entity별 비즈니스 로직 리팩터링
- Issue #39와 관계없는 기존 코드 정리

## 요구사항 변경 요약

- `docs/requirements.md` 변경 없음
- 사유:
  - 사용자에게 제공되는 기능이 변경되지 않는다.
  - 기존 생성·수정 시각 관리 책임을 JPA Auditing으로 이전할 뿐
    비즈니스 규칙과 외부 동작은 유지된다.
- 참조: `docs/requirements.md`

## API 변경 요약

- `docs/api-spec.md` 변경 없음
- 사유:
  - API Method와 URL이 변경되지 않는다.
  - Request/Response DTO가 변경되지 않는다.
  - 기존 응답 시간 필드의 타입, 형식 및 의미가 유지된다.
  - 신규 상태 코드나 예외 응답이 추가되지 않는다.
- 참조: `docs/api-spec.md`

## ERD 변경

- ERD 변경 없음
- 사유:
  - 기존 `id`, `created_at`, `updated_at` 컬럼을 그대로 사용한다.
  - 컬럼 타입, NULL 제약조건 및 ID 생성 전략을 변경하지 않는다.
  - 신규 테이블, 컬럼, FK, 인덱스 및 유니크 제약을 추가하지 않는다.
  - `BaseEntity`와 `BaseUpdatableEntity`는 `@MappedSuperclass`이므로
    별도 테이블로 매핑되지 않는다.
  - 변경 대상은 Java Entity의 상속 및 필드 선언 구조에 한정된다.
- 참조: `docs/erd.md`

## 공통 Entity 설계

### BaseEntity

- 전체 기존 Entity가 공통으로 가지는 ID와 생성 시각을 관리한다.
- 별도 테이블을 생성하지 않도록 `@MappedSuperclass`를 적용한다.
- JPA Auditing 콜백을 적용하기 위해
  `@EntityListeners(AuditingEntityListener.class)`를 사용한다.
- 공통 필드:
  - `id`
    - 타입: `Long`
    - `@Id`
    - `@GeneratedValue(strategy = GenerationType.IDENTITY)`
  - `createdAt`
    - 타입: `LocalDateTime`
    - `@CreatedDate`
    - NOT NULL
- 외부에서 기존과 동일하게 `getId()`, `getCreatedAt()`을 사용할 수 있어야 한다.

### BaseUpdatableEntity

- `BaseEntity`를 상속한다.
- 별도 테이블을 생성하지 않도록 `@MappedSuperclass`를 적용한다.
- 추가 필드:
  - `updatedAt`
    - 타입: `LocalDateTime`
    - `@LastModifiedDate`
    - NOT NULL
- 외부에서 기존과 동일하게 `getUpdatedAt()`을 사용할 수 있어야 한다.
- 최초 저장 시에도 `updatedAt`이 설정되어 기존 동작을 유지한다.

## JPA Auditing 설정

- 애플리케이션 설정에 JPA Auditing을 활성화한다.
- 최초 저장 시 수정 시각도 설정되도록 `modifyOnCreate=true` 동작을 유지한다.
- 날짜만 관리하므로 `AuditorAware`는 추가하지 않는다.
- 기본 시스템 시간을 사용하며 별도의 시간대 또는
  `DateTimeProvider` 정책은 도입하지 않는다.

## Entity 적용 범위

### BaseUpdatableEntity 적용

다음 Entity는 `id`, `createdAt`, `updatedAt`을 상속한다.

- `Category`
- `User`
- `Post`
- `Comment`

각 Entity에서 다음 항목을 제거한다.

- 중복된 `id`, `createdAt`, `updatedAt` 필드 선언
- 생성 메서드의 생성·수정 시각 직접 할당
- 수정·삭제 메서드의 수정 시각 직접 할당
- 제거된 필드에만 사용되던 import

### BaseEntity 적용

다음 Entity는 `id`, `createdAt`을 상속한다.

- `PostFile`
- `PostLike`
- `CommentLike`

각 Entity에서 다음 항목을 제거한다.

- 중복된 `id`, `createdAt` 필드 선언
- 생성 메서드의 생성 시각 직접 할당
- 제거된 필드에만 사용되던 import

## 기존 동작 유지 조건

- 모든 Entity의 ID는 기존과 동일하게 `Long` 타입을 사용한다.
- ID 생성 전략은 기존과 동일하게 `GenerationType.IDENTITY`를 사용한다.
- 기존 테이블명과 컬럼명을 유지한다.
- 기존 NOT NULL 제약을 유지한다.
- 기존 Entity 생성 메서드의 호출 방식은 변경하지 않는다.
- 기존 Entity 수정·삭제 메서드의 비즈니스 동작을 유지한다.
- API 응답의 생성·수정 시각 형식과 의미를 유지한다.
- `PostRepositoryTest`의 상속 필드 접근과 정렬 검증이 유지되어야 한다.

## 테스트 및 검증

최소 검증은 생성 시각만 사용하는 Entity와 수정 시각까지 사용하는
Entity를 각각 대표하여 DB 기반 JPA 테스트로 수행한다.

### BaseEntity 검증

`PostLike` 등 `BaseEntity`를 직접 상속하는 Entity를 대표로 검증한다.

- 저장 전 수동 시간 할당 없이 저장할 수 있다.
- 저장 후 ID가 자동 생성된다.
- 저장 후 `createdAt`이 자동 설정된다.
- 저장 및 재조회 후 기존 테이블 매핑이 정상적으로 유지된다.

### BaseUpdatableEntity 검증

`Post` 등 `BaseUpdatableEntity`를 상속하는 Entity를 대표로 검증한다.

- 저장 후 ID가 자동 생성된다.
- 저장 후 `createdAt`과 `updatedAt`이 자동 설정된다.
- Entity 변경 후 flush 시 `updatedAt`이 갱신된다.
- Entity 변경 후에도 `createdAt`은 유지된다.
- 저장 및 재조회 후 기존 테이블 매핑이 정상적으로 유지된다.

### 회귀 검증

- 기존 Entity 및 Repository 테스트가 계속 통과해야 한다.
- 상속된 getter를 사용하는 기존 서비스와 테스트가 정상적으로 컴파일되어야 한다.
- 기본 검증 명령:

```powershell
.\gradlew.bat test
```

테스트 대상, 실행 결과 및 미검증 사항을
`docs/current-work-log.md`에 기록한다.

## 예상 변경 파일

### Architect 사전 반영 문서

- `docs/current-task.md`
  - Issue #39의 확정된 목표, 범위, 공통 Entity 설계,
    테스트 및 완료 조건 반영

`docs/requirements.md`, `docs/api-spec.md`, `docs/erd.md`는
실제 변경이 필요하지 않으므로 Architect 사전 반영 문서에 포함하지 않는다.

위 문서는 이번 초안 승인 후 Architect가 사전 반영한다.

### Builder 구현 변경 예상 파일

신규 공통 Entity 파일:

- `src/main/java/com/github/marcel615/askteacher/global/entity/BaseEntity.java`
- `src/main/java/com/github/marcel615/askteacher/global/entity/BaseUpdatableEntity.java`

JPA Auditing 설정:

- `src/main/java/com/github/marcel615/askteacher/global/config/JpaAuditingConfig.java`
  - JPA Auditing 활성화
  - 최초 저장 시 수정 시각 설정 동작 유지
  - MVC 슬라이스 테스트와 Auditing 설정 격리

기존 Entity 수정:

- `src/main/java/com/github/marcel615/askteacher/domain/category/entity/Category.java`
- `src/main/java/com/github/marcel615/askteacher/domain/user/entity/User.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/entity/Post.java`
- `src/main/java/com/github/marcel615/askteacher/domain/post/entity/PostFile.java`
- `src/main/java/com/github/marcel615/askteacher/domain/postlike/entity/PostLike.java`
- `src/main/java/com/github/marcel615/askteacher/domain/comment/entity/Comment.java`
- `src/main/java/com/github/marcel615/askteacher/domain/commentlike/entity/CommentLike.java`

Auditing 반영 시점 보완:

- `src/main/java/com/github/marcel615/askteacher/domain/post/service/PostService.java`
- `src/main/java/com/github/marcel615/askteacher/domain/comment/service/CommentService.java`
  - 수정 응답 생성 전 flush하여 갱신된 `updatedAt` 반영

신규 테스트 파일:

- `src/test/java/com/github/marcel615/askteacher/global/entity/JpaAuditingRepositoryTest.java`
  - `BaseEntity` 생성 시각 자동 설정 검증
  - `BaseUpdatableEntity` 생성·수정 시각 자동 설정 검증
  - 생성 시각 불변 검증
  - 기존 DB 매핑 유지 검증

기존 테스트 보완:

- `src/test/java/com/github/marcel615/askteacher/support/RepositoryTestSupport.java`
- `src/test/java/com/github/marcel615/askteacher/domain/comment/service/CommentServiceTest.java`
- `src/test/java/com/github/marcel615/askteacher/domain/post/repository/PostFileRepositoryTest.java`
- `src/test/java/com/github/marcel615/askteacher/domain/post/service/PostServiceTest.java`
- `src/test/java/com/github/marcel615/askteacher/domain/post/storage/PostFileStorageTest.java`
- `src/test/java/com/github/marcel615/askteacher/integration/PostServiceIntegrationTest.java`
  - JPA Auditing의 persist·flush 시점에 맞춰 기존 검증 보완

위 서비스 및 기존 테스트 변경은 구현 중 예상과 다른 Auditing 동작을
보고한 뒤 사용자 승인에 따라 추가 반영했다.

작업 기록:

- `docs/current-work-log.md`
  - Issue #39 기준으로 초기화
  - 주요 결정, 테스트 결과 및 미검증 사항 기록

위 파일 외 변경이 필요하면 Builder가 사유와 범위를 먼저 보고하고
사용자 승인을 받은 후 진행한다.

## 구현 순서

1. `BaseEntity` 구현
2. `BaseUpdatableEntity` 구현
3. JPA Auditing 활성화
4. 생성 시각만 사용하는 Entity에 `BaseEntity` 적용
5. 수정 시각까지 사용하는 Entity에 `BaseUpdatableEntity` 적용
6. 각 Entity의 중복 필드와 수동 시간 할당 제거
7. JPA Auditing Repository 테스트 작성
8. 기존 관련 테스트 실행
9. 전체 테스트 실행
10. 테스트 결과 및 미검증 사항 기록

## 완료 조건

- [ ] `BaseEntity`가 구현되어 있다.
- [ ] `BaseUpdatableEntity`가 구현되어 있다.
- [ ] JPA Auditing이 활성화되어 있다.
- [ ] 전체 기존 Entity에 적절한 기반 클래스가 적용되어 있다.
- [ ] Entity의 중복 ID 및 시간 필드 선언이 제거되어 있다.
- [ ] Entity의 수동 생성·수정 시각 할당이 제거되어 있다.
- [ ] 저장 시 ID와 `createdAt`이 자동 설정된다.
- [ ] 저장 시 `updatedAt`이 자동 설정된다.
- [ ] 수정 시 `updatedAt`이 자동 갱신된다.
- [ ] 수정 후에도 `createdAt`이 유지된다.
- [ ] 기존 테이블 및 컬럼 매핑이 유지된다.
- [ ] API 요청·응답 동작이 변경되지 않는다.
- [ ] 최소 Repository 테스트가 작성되어 있다.
- [ ] 기존 테스트와 `.\gradlew.bat test`가 통과한다.
- [ ] 테스트 범위, 실행 결과 및 미검증 사항이 기록되어 있다.
