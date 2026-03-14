package com.project.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.api.dto.guardian.GuardianRequest;
import com.project.api.dto.guardian.GuardianResponse;
import com.project.api.exception.BusinessException;
import com.project.api.exception.GlobalExceptionHandler;
import com.project.api.security.JwtAuthenticationFilter;
import com.project.api.security.JwtTokenProvider;
import com.project.api.service.GuardianService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GuardianController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("GuardianController 테스트")
class GuardianControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GuardianService guardianService;

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
    @DisplayName("POST /api/guardians")
    class AddGuardian {

        @Test
        @DisplayName("보호자 등록 성공")
        void addGuardian_success() throws Exception {
            // Arrange
            GuardianRequest request = new GuardianRequest("김보호", "01098765432", "부모");
            GuardianResponse response = new GuardianResponse(1L, "김보호", "01098765432", "부모");
            given(guardianService.addGuardian(eq(1L), any(GuardianRequest.class))).willReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/guardians")
                            .with(authentication(createAuth(1L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("보호자 등록 완료"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("김보호"))
                    .andExpect(jsonPath("$.data.phone").value("01098765432"))
                    .andExpect(jsonPath("$.data.relationship").value("부모"));
        }

        @Test
        @DisplayName("보호자 5명 초과 등록 시 400 응답")
        void addGuardian_maxLimitReached_badRequest() throws Exception {
            // Arrange
            GuardianRequest request = new GuardianRequest("김보호", "01098765432", "부모");
            given(guardianService.addGuardian(eq(1L), any(GuardianRequest.class)))
                    .willThrow(BusinessException.badRequest("보호자는 최대 5명까지 등록할 수 있습니다"));

            // Act & Assert
            mockMvc.perform(post("/api/guardians")
                            .with(authentication(createAuth(1L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("보호자는 최대 5명까지 등록할 수 있습니다"));
        }

        @Test
        @DisplayName("보호자 이름 누락 시 400 응답")
        void addGuardian_missingName_badRequest() throws Exception {
            // Arrange
            String requestBody = """
                    {"name": "", "phone": "01098765432", "relationship": "부모"}
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/guardians")
                            .with(authentication(createAuth(1L)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/guardians")
    class GetGuardians {

        @Test
        @DisplayName("보호자 목록 조회 성공")
        void getGuardians_success() throws Exception {
            // Arrange
            List<GuardianResponse> guardians = List.of(
                    new GuardianResponse(1L, "김보호1", "01011111111", "부모"),
                    new GuardianResponse(2L, "김보호2", "01022222222", "형제")
            );
            given(guardianService.getGuardians(1L)).willReturn(guardians);

            // Act & Assert
            mockMvc.perform(get("/api/guardians")
                            .with(authentication(createAuth(1L))))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].name").value("김보호1"))
                    .andExpect(jsonPath("$.data[1].name").value("김보호2"));
        }

        @Test
        @DisplayName("보호자가 없을 때 빈 리스트 반환")
        void getGuardians_empty() throws Exception {
            // Arrange
            given(guardianService.getGuardians(1L)).willReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/api/guardians")
                            .with(authentication(createAuth(1L))))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("DELETE /api/guardians/{guardianId}")
    class RemoveGuardian {

        @Test
        @DisplayName("보호자 삭제 성공")
        void removeGuardian_success() throws Exception {
            // Arrange
            willDoNothing().given(guardianService).removeGuardian(1L, 1L);

            // Act & Assert
            mockMvc.perform(delete("/api/guardians/1")
                            .with(authentication(createAuth(1L))))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("보호자 삭제 완료"));
        }

        @Test
        @DisplayName("존재하지 않는 보호자 삭제 시 404 응답")
        void removeGuardian_notFound() throws Exception {
            // Arrange
            willThrow(BusinessException.notFound("보호자를 찾을 수 없습니다"))
                    .given(guardianService).removeGuardian(1L, 999L);

            // Act & Assert
            mockMvc.perform(delete("/api/guardians/999")
                            .with(authentication(createAuth(1L))))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("보호자를 찾을 수 없습니다"));
        }
    }
}
