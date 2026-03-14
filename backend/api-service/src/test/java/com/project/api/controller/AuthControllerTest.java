package com.project.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.api.dto.auth.LoginRequest;
import com.project.api.dto.auth.LoginResponse;
import com.project.api.dto.auth.SignupRequest;
import com.project.api.exception.BusinessException;
import com.project.api.exception.GlobalExceptionHandler;
import com.project.api.security.JwtAuthenticationFilter;
import com.project.api.security.JwtTokenProvider;
import com.project.api.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("AuthController 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Nested
    @DisplayName("POST /api/auth/signup")
    class Signup {

        @Test
        @DisplayName("유효한 요청으로 회원가입 성공")
        void signup_success() throws Exception {
            // Arrange
            SignupRequest request = new SignupRequest("홍길동", "test@example.com", "password123", "01012345678");
            LoginResponse response = new LoginResponse("jwt-token", 1L, "홍길동", "test@example.com", "ROLE_USER");
            given(authService.signup(any(SignupRequest.class))).willReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("회원가입 성공"))
                    .andExpect(jsonPath("$.data.token").value("jwt-token"))
                    .andExpect(jsonPath("$.data.userId").value(1))
                    .andExpect(jsonPath("$.data.name").value("홍길동"))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"))
                    .andExpect(jsonPath("$.data.role").value("ROLE_USER"));
        }

        @Test
        @DisplayName("이미 등록된 이메일로 가입 시 409 응답")
        void signup_duplicateEmail_conflict() throws Exception {
            // Arrange
            SignupRequest request = new SignupRequest("홍길동", "existing@example.com", "password123", "01012345678");
            given(authService.signup(any(SignupRequest.class)))
                    .willThrow(BusinessException.conflict("이미 등록된 이메일입니다: existing@example.com"));

            // Act & Assert
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("이미 등록된 이메일입니다: existing@example.com"));
        }

        @Test
        @DisplayName("이메일 누락 시 400 응답 (유효성 검증 실패)")
        void signup_missingEmail_badRequest() throws Exception {
            // Arrange - email 필드가 빈 문자열
            String requestBody = """
                    {"name": "홍길동", "email": "", "password": "password123", "phone": "01012345678"}
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("비밀번호 8자 미만 시 400 응답")
        void signup_shortPassword_badRequest() throws Exception {
            // Arrange
            String requestBody = """
                    {"name": "홍길동", "email": "test@example.com", "password": "short", "phone": "01012345678"}
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("이름 누락 시 400 응답")
        void signup_missingName_badRequest() throws Exception {
            // Arrange
            String requestBody = """
                    {"name": "", "email": "test@example.com", "password": "password123", "phone": "01012345678"}
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("유효한 자격증명으로 로그인 성공")
        void login_success() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("test@example.com", "password123");
            LoginResponse response = new LoginResponse("jwt-token", 1L, "홍길동", "test@example.com", "ROLE_USER");
            given(authService.login(any(LoginRequest.class))).willReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("로그인 성공"))
                    .andExpect(jsonPath("$.data.token").value("jwt-token"))
                    .andExpect(jsonPath("$.data.userId").value(1))
                    .andExpect(jsonPath("$.data.name").value("홍길동"));
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 401 응답")
        void login_wrongPassword_unauthorized() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
            given(authService.login(any(LoginRequest.class)))
                    .willThrow(BusinessException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다"));

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다"));
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 401 응답")
        void login_nonExistentUser_unauthorized() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("nobody@example.com", "password123");
            given(authService.login(any(LoginRequest.class)))
                    .willThrow(BusinessException.unauthorized("이메일 또는 비밀번호가 올바르지 않습니다"));

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("이메일 형식이 잘못된 경우 400 응답")
        void login_invalidEmailFormat_badRequest() throws Exception {
            // Arrange
            String requestBody = """
                    {"email": "invalid-email", "password": "password123"}
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
