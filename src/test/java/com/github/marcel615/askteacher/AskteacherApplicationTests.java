package com.github.marcel615.askteacher;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class AskteacherApplicationTests {
    @org.junit.jupiter.api.Nested
    @org.junit.jupiter.api.DisplayName("애플리케이션 시작")
    class Scenarios {

        @org.junit.jupiter.api.DisplayName("애플리케이션 컨텍스트가 정상적으로 시작된다")
        @Test
        void contextLoads() {
        }

    }
}
