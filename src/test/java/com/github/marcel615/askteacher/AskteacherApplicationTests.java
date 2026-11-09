package com.github.marcel615.askteacher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class AskteacherApplicationTests {
    @Nested
    @DisplayName("애플리케이션 시작")
    class Scenarios {

        @DisplayName("애플리케이션 컨텍스트가 정상적으로 시작된다")
        @Test
        void contextLoads() {
        }

    }
}
