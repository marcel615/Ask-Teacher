# Current Work Log

## 2026-11-14 — Issue #79 로그인·로그아웃 보안 처리 구조 정리

- 사용자 요청 `ask-teacher 작업 시작하자`에 따라 Main이 유일한 Backend Ready Issue #79를 선정했다. 우선순위 미설정·Blocked by 없음·진행 중 작업/열린 PR 없음 확인 후 Project를 In Progress로 갱신·재조회했다.
- 이전 문서정비 PR #75 merge와 해당 devlog 생략 사용자 승인 기록, 이전 Issue #58 devlog 반영을 Main이 확인한 뒤 임시 작업 기록을 전환했다.
- Main 설계 게이트 통과 범위는 docs/current-task.md이며 브랜치는 feature/issue-79-security-auth-refactor → develop, 기준 commit c6398bf03319f73002b6e01af8c21f221e7438f4다.
- 공식 Specs main 확인 SHA는 06ccca533c1120dcc9696a89c9b2a7e71695369f다. 외부 계약·인증 정책·인가 규칙·DB·운영 설정은 변경하지 않는다. Issue 본문 속 검증 예외는 사용자 코멘트 원본이 없어 적용하지 않고 기본 전체 테스트·독립 리뷰·CI·devlog 절차를 유지한다.
- JSON 로그인 필터와 성공/실패 핸들러, 로그아웃 필터/핸들러를 SecurityConfig에 연결하고 토큰 발급 정보·registry·쿠키·401/403 응답 책임을 분리한다. 기존 RedisSessionStore와 Lua/키/TTL/원자성을 그대로 재사용하는 위임 구현으로 계약 변경 없이 범위를 충족한다. 기본 formLogin/LogoutFilter는 JSON 응답·JWT 검증 순서·Redis 장애 응답 계약 때문에 사용하지 않는다.
- 테스트 환경: Corretto 17.0.21, 기존 test 프로필의 격리 H2·임시 파일 경로·Redis 127.0.0.1:16379를 사용한다. Docker daemon은 사용자가 시작했고 Main이 테스트 전용 askteacher-issue79-test-redis(redis:7)를 준비해 PONG을 확인했다. 기존 개발 Redis(6379)는 사용하지 않는다. 임시 Redis lifecycle은 Main이 관리한다.
- 현재 구현·테스트 보완 중이며 아직 검증 통과로 간주하지 않는다.
- 초기 검증: Corretto 17.0.21에서 `.\gradlew.bat compileTestJava --offline` 성공 후 `.\gradlew.bat test --offline` 전체 성공(37초). JUnit XML 집계 183건, 실패·오류·건너뜀 0건. RedisSessionStore와 Lua 원본 diff 0이며 git diff --check 통과했다.
- 이후 자체 점검에서 새 로그인 필터가 Content-Type에 무관하게 JSON을 읽어 기존 MVC보다 허용 범위를 넓힐 가능성을 발견했다. 수정 전 실제 MVC 응답을 확인하려고 임시 LoginMediaTypeBaselineTest를 추가했으나 WebMvcTest가 전체 Controller를 로드해 AuthService 빈 없음으로 실패했다. 대상 Controller 한정 지정을 시도한 수정이 줄바꿈 치환 문제로 반영되지 않았고 두 차례 재실행에도 같은 실패가 남아 수정·재검증 2회 한도에 따라 Main에 보고하고 중단했다.
- 임시 baseline 테스트는 아직 소스에 남아 있으며 마지막 test 결과 디렉터리는 임시 테스트 1건 실패 결과로 덮였다. 직전 제품 전체 183건 성공은 실행 출력과 당시 XML 집계로 확인한 사실이나, 현재 소스 전체의 최종 성공 근거로 사용하지 않는다. Content-Type 경계 보존 수정과 검증은 미완료다.

- Main 중단 게이트: 구현 게이트 보류. 현재 feature/issue-79-security-auth-refactor, HEAD c6398bf03319f73002b6e01af8c21f221e7438f4이며 변경은 미커밋으로 보존한다. commit·push·PR·독립 리뷰·devlog는 아직 수행하지 않았다. Project #79는 In Progress 상태다. 임시 진단 테스트 컨텍스트 수정과 Content-Type 경계 보존·전체 재검증은 사용자 재개 지시 전 보류한다.
- Main은 이번 작업에서 생성한 askteacher-issue79-test-redis 컨테이너만 stop/rm하여 정리했다. Docker Desktop 종료나 다른 컨테이너 stop/rm은 실행하지 않았다.
- 2026-11-14 사용자 추가 지시 `뭐 그렇게 해봐`에 따라 Main이 추가 수정·검증 재개를 승인했다. 이번 재개의 수정·재검증 한도는 최대 2회다. 진단용 별도 baseline 컨텍스트를 제거하고 기존 AuthSecurityFilterTest에서 필요한 미디어 타입 경계만 검증한다.
- 이전 실패 요약 정정: 재개 시 실제 임시파일에는 Controller 한정이 적용돼 있었으며 마지막 JUnit XML의 원인은 AuthService가 아니라 JwtAuthenticationFilter의 JwtTokenProvider 빈 누락이었다. 최초 실패와 후속 진단 컨텍스트의 실패 원인을 동일하게 요약한 기록은 부정확했다. 제품 전체 183건 성공과 후속 진단 테스트 실패를 구분한다. 임시 LoginMediaTypeBaselineTest는 재개 승인 범위에서 제거했다.
- 이전 MVC 경계의 코드 근거: 캐시된 Spring web/webmvc 7.0.7 sources의 AbstractMessageConverterMethodArgumentResolver는 무타입을 application/octet-stream으로 대체하며 무타입+빈본문을 required-body 오류로 전달한다. RequestResponseBodyMethodProcessor의 required body 오류는 400, JSON converter는 application/json 및 application/*+json만 지원한다. 그 외 미디어 타입은 HttpMediaTypeNotSupportedException이고 현재 GlobalExceptionHandler의 일반 Exception 처리가 500을 반환한다. 이 기존 문제를 이번 구조 변경에서 415로 바꾸지 않는다.
- 로그인 필터는 같은 JacksonJsonHttpMessageConverter를 재사용하여 미디어 타입·charset·JSON 파싱의 허용 경계를 유지한다. 무타입+빈본문 400과 그 외 미지원 타입 500의 기존 분기를 보존한다. Accept 응답 협상 차이는 추가 검토 중이며 아직 최종 게이트 통과로 간주하지 않는다.
- 재개 후 검증 1회: `.\gradlew.bat test --offline --tests '*AuthSecurityFilterTest' --tests '*AuthServiceTest' --tests '*AuthControllerTest' --tests '*JwtAuthenticationFilterTest'` 성공(컴파일 포함 15초), 관련 38건 실패·오류·건너뜀 0건. 이어 기본 명령 `.\gradlew.bat test` 전체 성공(37초), JUnit XML 합계 188건 실패·오류·건너뜀 0건이다. 재개 후 테스트·컴파일 실패는 없다.
- Content-Type 회귀 검증은 JSON과 application/*+json 성공, text/plain JSON 본문 및 명시한 빈 XML의 기존 500, 무타입 JSON 본문의 기존 500, 무타입+빈본문의 400을 포함한다. 임시 baseline 테스트 소스·class는 제거됐고 최종 전체 실행에 포함되지 않는다.
- 공식 API 원본 SHA 06ccca533c1120dcc9696a89c9b2a7e71695369f에서 Accept 언급은 없고 로그인 JSON 및 재발급의 동일 JSON 응답을 규정한다. 비JSON Accept의 구체 상태는 규정되지 않았다. 새 필터의 직접 JSON 응답과 기존 MVC 협상 간 비JSON Accept 동작 차이는 미검증이며, Main 지시에 따라 협상 설계를 확대하지 않고 독립 리뷰·PR의 한계로 남긴다.
- 최종 diff 검증: 기존 RedisSessionStore(Lua 3개·키·TTL·원자성) diff 0, DB·환경 설정·공식 Specs·AI-DLC 운영 파일 변경 없음, git diff --check 통과. AuthService의 Authentication import는 로그아웃 검증 결과에서 실제 사용한다. 기존 메서드 사이 빈줄을 정리했다. 테스트 전용 Redis 정리는 Main이 담당하며 commit·push·PR·독립 리뷰·devlog·최종 head CI는 Main의 후속 단계다.
- Main 구현 게이트 통과: 승인 설계와 제품·테스트 diff 일치, JUnit XML 188건 실패·오류·건너뜀 0건을 직접 집계했고 git diff --check 통과 및 보호 파일 diff 없음을 확인했다. 비JSON Accept 응답 협상은 독립 리뷰에서 한계를 확인한다. 다음 단계는 commit·push·PR 및 Architect 독립 리뷰다.
- Main이 구현 commit a7933069e995439207e773df8f9c9919e35b16ea를 origin에 push하고 PR #80(https://github.com/marcel615/Ask-Teacher-spring/pull/80)을 develop 대상으로 생성했다. PR은 Backend #79만 종료하며 Parent 자동 종료 참조가 없다. Project #79 In Review 전환·재조회 성공. 최초 head의 CI test 실행 중, mergeable은 MERGEABLE이며 최종 머지 준비 판정은 아직 대기다. Architect 독립 리뷰를 요청했다.
- Architect 독립 리뷰의 필수 P2 지적: 로그인 성공 핸들러에서 발급 중 일반 DB/런타임 오류가 기존 MVC 일반 예외 처리의 500 JSON을 우회했다. Main 승인으로 서비스 발급 호출만 RuntimeException 경계로 감싸 기존 500 JSON으로 반환하고, 기존 RedisUnavailableException/CustomException 분기는 유지했다. Refresh 쿠키 생성과 응답 쓰기는 catch 범위 밖으로 두어 응답 쓰기 실패를 재작성하지 않는다.
- 리뷰 회귀 테스트는 service.issueFor의 DataAccessResourceFailureException에 대해 500 JSON·기존 한국어 메시지·Refresh 쿠키 미발급을 확인한다. 이번 리뷰 수정·재검증은 재개 후 2회째이며 테스트 결과 확인 전 통과로 간주하지 않는다. 기존 Main의 PR/Project 기록을 보존했다.
- Architect P2 수정 검증 결과: `.\gradlew.bat test --offline --tests '*AuthSecurityFilterTest' --tests '*AuthServiceTest' --tests '*AuthRedisFailureIntegrationTest'` 33건 성공(21초), 이어 기본 `.\gradlew.bat test` 전체 189건 성공(36초). 두 실행의 실패·오류·건너뜀은 모두 0건이다. 이번 수정·재검증에서 실패는 없었고 git diff --check 통과를 확인했다. 비JSON Accept 한계는 유지하며 계약·운영·환경·DB·RedisSessionStore를 변경하지 않았다.
- Architect 재리뷰 통과: 발급 일반 오류의 기존 500 JSON·Redis503/Custom 예외 유지와 응답 쓰기 경계 분리를 확인했다. DB 장애 회귀 테스트 및 XML 189건 실패·오류·건너뜀 0건을 직접 확인했고 추가 필수 지적은 없다. Main도 수정 diff와 XML·diff --check를 확인했다. 첫 PR head a793306의 CI test는 SUCCESS였으나 최종 push head 기준으로 다시 확인한다.
- 독립 추적이 필요한 기술 후속 Issue나 신규 기능 Draft는 생성하지 않았다. 이번 책임 분리에서 새 독립 작업의 필요성이 확인되지 않았고 기존 미지원 Content-Type 500·비JSON Accept/React/운영 Redis 한계는 PR과 기록에 남긴다. Reporter에 월별 devlog 초안을 요청했다.
- 리뷰 수정 commit 2f00930과 Reporter 월별 devlog append를 Main이 확인했다. 기존 devlog 내용 불변·추가항목 근거·diff --check를 확인해 리뷰·기록 게이트 통과. 최종 제품 전체 테스트 189건 성공 이후 제품 변경은 없고 이번 최종 commit은 devlog·작업 기록만 포함한다. 테스트 전용 Redis는 Main이 stop/rm 완료했다.
- 최종 push 뒤 추적 파일을 수정하지 않고 실제 GitHub head의 CI·리뷰·보호 조건·충돌·Project 상태를 다시 확인한다. PR #80은 사용자 직접 merge 대기이며 merge 확인 전 feature 브랜치는 보존한다. 최종 CI 결과는 PR과 대화 보고를 재개 근거로 사용한다.
