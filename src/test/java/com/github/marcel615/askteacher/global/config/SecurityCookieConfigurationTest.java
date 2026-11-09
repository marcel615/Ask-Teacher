package com.github.marcel615.askteacher.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.assertj.core.api.Assertions.*;

class SecurityCookieConfigurationTest {
    @Nested
    @DisplayName("운영 쿠키 설정")
    class Scenarios {
        @DisplayName("개발이나 테스트 프로필이 아니면 비보안 쿠키를 거부한다")
        @Test void secureCookieRequiredOutsideDevelopment() {
            var env = new MockEnvironment();
            assertThatThrownBy(() -> new SecurityConfig(null, false, "Strict", "https://app.example", "test-only-secret-not-used-here-32-bytes", env))
                    .isInstanceOf(IllegalStateException.class);
        }
        @DisplayName("SameSite None에서는 개발 환경도 Secure가 필요하다")
        @Test void sameSiteNoneRequiresSecure() {
            var env = new MockEnvironment(); env.setActiveProfiles("dev");
            assertThatThrownBy(() -> new SecurityConfig(null, false, "None", "https://app.example", "test-only-secret-not-used-here-32-bytes", env))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
