package com.github.marcel615.askteacher.domain.auth.service;

import com.github.marcel615.askteacher.domain.auth.dto.request.SignupRequest;
import com.github.marcel615.askteacher.domain.auth.dto.response.LoginResponse;
import com.github.marcel615.askteacher.domain.auth.dto.response.SignupResponse;
import com.github.marcel615.askteacher.global.security.jwt.JwtRegistry;
import com.github.marcel615.askteacher.global.security.jwt.JwtInformation;
import com.github.marcel615.askteacher.domain.auth.session.RefreshToken;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import com.github.marcel615.askteacher.global.exception.CustomException;
import com.github.marcel615.askteacher.global.exception.ErrorCode;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetails;
import com.github.marcel615.askteacher.global.security.jwt.JwtTokenProvider;
import com.github.marcel615.askteacher.global.security.jwt.VerifiedAccessToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtTokenProvider jwtTokenProvider;
    @Mock
    JwtRegistry sessionStore;
    @InjectMocks
    AuthService authService;

    @Nested
    @DisplayName("회원가입")
    class Signup {
        @DisplayName("회원가입 시 비밀번호를 암호화하고 저장된 사용자를 반환한다")
        @Test
        void signupEncodesPasswordAndMapsSavedUser() {
            when(passwordEncoder.encode("password123")).thenReturn("encoded");
            when(userRepository.save(any())).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                assertThat(user.getPassword()).isEqualTo("encoded");
                ReflectionTestUtils.setField(user, "id", 12L);
                return user;
            });
            SignupRequest request = new SignupRequest("a@example.com", "password123", "nickname");

            SignupResponse response = authService.signup(request);

            assertThat(response)
                    .isEqualTo(new SignupResponse(12L, "a@example.com", "nickname"));
        }

        @DisplayName("이메일이 중복되면 저장하지 않는다")
        @Test
        void duplicateEmailStopsBeforeSaving() {
            when(userRepository.existsByEmail("a@example.com")).thenReturn(true);
            SignupRequest request = new SignupRequest("a@example.com", "password123", "nick");

            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
            verifyNoInteractions(passwordEncoder, jwtTokenProvider);
            verify(userRepository, never()).save(any());
        }

        @DisplayName("닉네임이 중복되면 저장하지 않는다")
        @Test
        void duplicateNicknameStopsBeforeSaving() {
            when(userRepository.existsByNickname("nick")).thenReturn(true);
            SignupRequest request = new SignupRequest("a@example.com", "password123", "nick");

            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.DUPLICATE_NICKNAME);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {
        @DisplayName("일치하는 사용자의 로그인에 토큰을 발급한다")
        @Test
        void loginIssuesTokenForMatchedUser() {
            User user = User.createUser("a@example.com", "encoded", "nick");
            ReflectionTestUtils.setField(user, "id", 12L);
            AskTeacherUserDetails principal = new AskTeacherUserDetails(
                    12L,
                    user.getEmail(),
                    "encoded",
                    "USER"
            );
            when(sessionStore.login(12L)).thenReturn(new RefreshToken(12L, "session", "refresh"));
            when(userRepository.findById(12L)).thenReturn(Optional.of(user));
            when(jwtTokenProvider.createAccessToken(12L, user.getRole(), "session")).thenReturn("token");
            LoginResponse expectedResponse = new LoginResponse("token", "Bearer", 900);
            JwtInformation expectedTokens = new JwtInformation(expectedResponse, "refresh");

            JwtInformation issuedTokens = authService.issueFor(principal);

            assertThat(issuedTokens).isEqualTo(expectedTokens);
        }

    }

    @Nested
    @DisplayName("재발급")
    class Refresh {
        @DisplayName("Redis 연결 실패를 인증 만료와 구별한다")
        @Test
        void refreshMapsRedisFailure() {
            when(sessionStore.rotate("refresh")).thenThrow(new RedisConnectionFailureException("unavailable"));
            assertThatThrownBy(() -> authService.refresh("refresh")).isInstanceOf(RedisUnavailableException.class);
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {
        @DisplayName("로그아웃은 검증 결과를 재사용하여 만료 경계에서 재파싱하지 않는다")
        @Test
        void logoutUsesOneVerification() {
            AskTeacherUserDetails principal = new AskTeacherUserDetails(12L, "", "", "USER");
            UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                    principal,
                    null,
                    principal.getAuthorities()
            );
            VerifiedAccessToken verifiedAccessToken = new VerifiedAccessToken(authentication, "sid");

            authService.logout(null, verifiedAccessToken);

            verifyNoInteractions(jwtTokenProvider);
            verify(sessionStore).logoutAccess(12L, "sid");
        }
    }
}
