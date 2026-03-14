package com.project.api.service;

import com.project.api.domain.User;
import com.project.api.dto.auth.LoginRequest;
import com.project.api.dto.auth.LoginResponse;
import com.project.api.dto.auth.SignupRequest;
import com.project.api.exception.BusinessException;
import com.project.api.repository.ProfileRepository;
import com.project.api.repository.UserRepository;
import com.project.api.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private User createTestUser() {
        return User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .name("홍길동")
                .phone("01012345678")
                .role(User.Role.ROLE_USER)
                .build();
    }

    @Nested
    @DisplayName("signup 메서드")
    class Signup {

        @Test
        @DisplayName("회원가입 성공 시 LoginResponse 반환")
        void signup_success() {
            // Arrange
            SignupRequest request = new SignupRequest("홍길동", "test@example.com", "password123", "01012345678");
            User savedUser = createTestUser();

            given(userRepository.existsByEmail("test@example.com")).willReturn(false);
            given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(profileRepository.save(any())).willReturn(null);
            given(jwtTokenProvider.generateToken(any(), anyString(), anyString())).willReturn("jwt-token");

            // Act
            LoginResponse response = authService.signup(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("jwt-token");
            assertThat(response.email()).isEqualTo("test@example.com");
            assertThat(response.name()).isEqualTo("홍길동");
            verify(userRepository).save(any(User.class));
            verify(profileRepository).save(any());
        }

        @Test
        @DisplayName("이미 등록된 이메일로 가입 시 CONFLICT 예외 발생")
        void signup_duplicateEmail_throwsBusinessException() {
            // Arrange
            SignupRequest request = new SignupRequest("홍길동", "existing@example.com", "password123", "01012345678");
            given(userRepository.existsByEmail("existing@example.com")).willReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이미 등록된 이메일입니다")
                    .extracting("status")
                    .isEqualTo(HttpStatus.CONFLICT);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login 메서드")
    class Login {

        @Test
        @DisplayName("유효한 자격증명으로 로그인 성공")
        void login_success() {
            // Arrange
            LoginRequest request = new LoginRequest("test@example.com", "password123");
            User user = createTestUser();

            given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
            given(jwtTokenProvider.generateToken(any(), anyString(), anyString())).willReturn("jwt-token");

            // Act
            LoginResponse response = authService.login(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("jwt-token");
            assertThat(response.email()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 UNAUTHORIZED 예외 발생")
        void login_wrongPassword_throwsBusinessException() {
            // Arrange
            LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
            User user = createTestUser();

            given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrongpassword", "encodedPassword")).willReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다")
                    .extracting("status")
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 UNAUTHORIZED 예외 발생")
        void login_nonExistentEmail_throwsBusinessException() {
            // Arrange
            LoginRequest request = new LoginRequest("nobody@example.com", "password123");
            given(userRepository.findByEmail("nobody@example.com")).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다")
                    .extracting("status")
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("소셜 로그인 계정으로 비밀번호 로그인 시 예외 발생")
        void login_socialAccount_throwsBusinessException() {
            // Arrange - password가 null인 소셜 로그인 유저
            LoginRequest request = new LoginRequest("social@example.com", "password123");
            User socialUser = User.builder()
                    .email("social@example.com")
                    .password(null)
                    .name("소셜유저")
                    .role(User.Role.ROLE_USER)
                    .build();

            given(userRepository.findByEmail("social@example.com")).willReturn(Optional.of(socialUser));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("소셜 로그인으로 가입된 계정입니다")
                    .extracting("status")
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }
}
