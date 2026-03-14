package com.project.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.api.dto.checkin.CheckInRequest;
import com.project.api.dto.checkin.CheckInResponse;
import com.project.api.exception.BusinessException;
import com.project.api.exception.GlobalExceptionHandler;
import com.project.api.security.JwtAuthenticationFilter;
import com.project.api.security.JwtTokenProvider;
import com.project.api.service.CheckInService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@WebMvcTest(CheckInController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("CheckInController 테스트")
class CheckInControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CheckInService checkInService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UsernamePasswordAuthenticationToken createAuth(Long userId) {
        return new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Nested
    @DisplayName("POST /api/checkins")
    class CheckIn {

        @Test
        @DisplayName("인증된 사용자의 체크인 성공")
        void checkIn_success() throws Exception {
            // Arrange
            CheckInRequest request = new CheckInRequest("오늘 기분 좋아요", 8);
            CheckInResponse response = new CheckInResponse(
                    1L, 1L, "홍길동", "CHECKED", "오늘 기분 좋아요", 8, LocalDateTime.now());
            given(checkInService.checkIn(eq(1L), any(CheckInRequest.class))).willReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/checkins")
                            .with(authentication(createAuth(1L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("안부 체크 완료"))
                    .andExpect(jsonPath("$.data.status").value("CHECKED"))
                    .andExpect(jsonPath("$.data.message").value("오늘 기분 좋아요"))
                    .andExpect(jsonPath("$.data.moodScore").value(8));
        }

        @Test
        @DisplayName("Body 없이 체크인 성공 (기본 CheckInRequest 사용)")
        void checkIn_noBody_success() throws Exception {
            // Arrange
            CheckInResponse response = new CheckInResponse(
                    1L, 1L, "홍길동", "CHECKED", null, null, LocalDateTime.now());
            given(checkInService.checkIn(eq(1L), any(CheckInRequest.class))).willReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/checkins")
                            .with(authentication(createAuth(1L)))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("인증 없이 체크인 시 401 응답")
        void checkIn_unauthorized() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/checkins")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/checkins")
    class GetMyCheckIns {

        @Test
        @DisplayName("체크인 목록 페이지네이션 조회 성공")
        void getMyCheckIns_success() throws Exception {
            // Arrange
            List<CheckInResponse> items = List.of(
                    new CheckInResponse(2L, 1L, "홍길동", "CHECKED", "오후 체크인", 7, LocalDateTime.now()),
                    new CheckInResponse(1L, 1L, "홍길동", "CHECKED", "오전 체크인", 6, LocalDateTime.now().minusHours(4))
            );
            Page<CheckInResponse> page = new PageImpl<>(items, PageRequest.of(0, 20), 2);
            given(checkInService.getMyCheckIns(eq(1L), any())).willReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/checkins")
                            .with(authentication(createAuth(1L)))
                            .param("page", "0")
                            .param("size", "20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/checkins/latest")
    class GetLatestCheckIn {

        @Test
        @DisplayName("최신 체크인 조회 성공")
        void getLatestCheckIn_success() throws Exception {
            // Arrange
            CheckInResponse response = new CheckInResponse(
                    5L, 1L, "홍길동", "CHECKED", "최신 체크인", 9, LocalDateTime.now());
            given(checkInService.getLatestCheckIn(1L)).willReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/checkins/latest")
                            .with(authentication(createAuth(1L))))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(5))
                    .andExpect(jsonPath("$.data.message").value("최신 체크인"));
        }

        @Test
        @DisplayName("체크인 기록 없을 때 404 응답")
        void getLatestCheckIn_notFound() throws Exception {
            // Arrange
            given(checkInService.getLatestCheckIn(1L))
                    .willThrow(BusinessException.notFound("체크인 기록이 없습니다"));

            // Act & Assert
            mockMvc.perform(get("/api/checkins/latest")
                            .with(authentication(createAuth(1L))))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("체크인 기록이 없습니다"));
        }
    }
}
