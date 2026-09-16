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

## 자동 API 테스트

- [ ] 새 API 또는 변경된 API의 Controller / Service / Repository 테스트와 통합 테스트가 작성되었는가?
- [ ] 요청 바인딩, Validation, 정상·실패 응답, 인증·인가 및 실제 DB 쿼리를 검증했는가?
- [ ] API별 시나리오와 대응 테스트 클래스가 기록되어 있고 기존 검증 범위를 유지했는가?
- [ ] 테스트 DB·인증 설정·파일 경로가 개발·운영 환경과 격리되고 데이터·파일이 정리되는가?
- [ ] 테스트가 고정 ID, 기존 데이터, 실행 순서, 수동 서버 실행에 의존하지 않는가?
- [ ] 자동 테스트 실행 결과와 미검증 사항이 기록되었는가?
- [ ] 기존 버그·명세 불일치를 기대값 변경이나 테스트 비활성화로 숨기지 않았는가?

## 리뷰 결과

### 반드시 수정

- 

### 선택 개선

- 

### 통과 항목

-
