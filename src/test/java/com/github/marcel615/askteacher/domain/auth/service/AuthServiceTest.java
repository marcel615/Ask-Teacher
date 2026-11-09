package com.github.marcel615.askteacher.domain.auth.service;

import com.github.marcel615.askteacher.domain.auth.dto.*;
import com.github.marcel615.askteacher.domain.user.entity.User;
import com.github.marcel615.askteacher.domain.user.repository.UserRepository;
import com.github.marcel615.askteacher.global.security.jwt.JwtTokenProvider;
import com.github.marcel615.askteacher.global.exception.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.github.marcel615.askteacher.domain.auth.session.RedisSessionStore;
import com.github.marcel615.askteacher.global.security.AskTeacherUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Nested
    @DisplayName("회원가입과 로그인")
    class Scenarios {
        @Mock UserRepository users;
        @Mock PasswordEncoder passwords;
        @Mock JwtTokenProvider tokens;
        @Mock AuthenticationManager authenticationManager;
        @Mock RedisSessionStore sessions;
        @InjectMocks AuthService service;

        @DisplayName("회원가입 시 비밀번호를 암호화하고 저장된 사용자를 반환한다")
        @Test void signupEncodesPasswordAndMapsSavedUser() {
            when(passwords.encode("password123")).thenReturn("encoded");
            when(users.save(any())).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                assertThat(user.getPassword()).isEqualTo("encoded");
                ReflectionTestUtils.setField(user, "id", 12L);
                return user;
            });
            assertThat(service.signup(new SignupRequest("a@example.com", "password123", "nickname")))
                    .isEqualTo(new SignupResponse(12L, "a@example.com", "nickname"));
        }

        @DisplayName("이메일이 중복되면 저장하지 않는다")
        @Test void duplicateEmailStopsBeforeSaving() {
            when(users.existsByEmail("a@example.com")).thenReturn(true);
            assertThatThrownBy(() -> service.signup(new SignupRequest("a@example.com", "password123", "nick")))
                    .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_EMAIL);
            verifyNoInteractions(passwords, tokens);
            verify(users, never()).save(any());
        }

        @DisplayName("닉네임이 중복되면 저장하지 않는다")
        @Test void duplicateNicknameStopsBeforeSaving() {
            when(users.existsByNickname("nick")).thenReturn(true);
            assertThatThrownBy(() -> service.signup(new SignupRequest("a@example.com", "password123", "nick")))
                    .isInstanceOf(CustomException.class).extracting("errorCode").isEqualTo(ErrorCode.DUPLICATE_NICKNAME);
            verify(users, never()).save(any());
        }

        @DisplayName("일치하는 사용자의 로그인에 토큰을 발급한다")
        @Test void loginIssuesTokenForMatchedUser() {
            User user = User.createUser("a@example.com", "encoded", "nick");
            ReflectionTestUtils.setField(user, "id", 12L);
            var principal = new AskTeacherUserDetails(12L, user.getEmail(), "encoded", "USER");
            when(authenticationManager.authenticate(any())).thenReturn(UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
            when(sessions.login(12L)).thenReturn(new RedisSessionStore.Token(12L, "session", "refresh"));
            when(users.findById(12L)).thenReturn(Optional.of(user));
            when(tokens.createAccessToken(12L, user.getRole(), "session")).thenReturn("token");
            assertThat(service.login(new LoginRequest(user.getEmail(), "password123")))
                    .isEqualTo(new AuthService.Issued(new LoginResponse("token", "Bearer", 900), "refresh"));
        }

        @DisplayName("이메일이 없거나 비밀번호가 틀리면 로그인을 거부한다")
        @Test void missingEmailAndWrongPasswordRejectLogin() {
            LoginRequest request = new LoginRequest("a@example.com", "wrong");
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
            assertThatThrownBy(() -> service.login(request)).extracting("errorCode").isEqualTo(ErrorCode.INVALID_LOGIN_INFO);
            assertThatThrownBy(() -> service.login(request)).extracting("errorCode").isEqualTo(ErrorCode.INVALID_LOGIN_INFO);
            verifyNoInteractions(tokens);
        }

        @DisplayName("Redis 연결 실패를 인증 만료와 구별한다")
        @Test void refreshMapsRedisFailure() {
            when(sessions.rotate("refresh")).thenThrow(new org.springframework.data.redis.RedisConnectionFailureException("unavailable"));
            assertThatThrownBy(() -> service.refresh("refresh")).isInstanceOf(RedisUnavailableException.class);
        }

        @DisplayName("로그아웃은 검증 결과를 재사용하여 만료 경계에서 재파싱하지 않는다")
        @Test void logoutUsesOneVerification() {
            var principal = new AskTeacherUserDetails(12L, "", "", "USER");
            var auth = UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
            service.logout(null, new JwtTokenProvider.Verified(auth, "sid"));
            verifyNoInteractions(tokens);
            verify(sessions).logoutAccess(12L, "sid");
        }
    }
}
