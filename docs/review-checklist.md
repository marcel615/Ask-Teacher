# Review Checklist

## 작업 범위

- [ ] `docs/current-task.md` 범위 안에서 작업했는가?
- [ ] 불필요한 변경이 없는가?
- [ ] 작업 브랜치가 `feature/*` 형식인가?
- [ ] PR 방향이 `feature/*` → `develop`인가?

## 백엔드 구조

- [ ] Controller / Service / Repository 책임이 분리되어 있는가?
- [ ] Entity를 API 응답으로 직접 반환하지 않는가?
- [ ] Request DTO와 Response DTO가 분리되어 있는가?
- [ ] Validation이 적용되어 있는가?
- [ ] 예외 응답 형식이 일관적인가?

## 설계 / 테스트

- [ ] API 구현이 `docs/api-spec.md`와 충돌하지 않는가?
- [ ] Entity 구현이 `docs/erd.md`와 충돌하지 않는가?
- [ ] `./gradlew test`가 통과했는가?

## 수동 API 확인

- [ ] 새 API 또는 변경된 API에 대한 `.http` 파일을 확인했는가?
- [ ] 가능한 범위에서 `.http` 수동 확인 결과를 기록했는가?
- [ ] `.http`가 자동 테스트를 대체하지 않는가?

## 리뷰 결과

### 반드시 수정

- 

### 선택 개선

- 

### 통과 항목

-
