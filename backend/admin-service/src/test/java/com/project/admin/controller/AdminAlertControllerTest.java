package com.project.admin.controller;

import com.project.admin.dto.AlertResponse;
import com.project.admin.exception.ResourceNotFoundException;
import com.project.admin.service.AdminAlertService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminAlertController.class)
@DisplayName("AdminAlertController 테스트")
class AdminAlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminAlertService adminAlertService;

    @Nested
    @DisplayName("GET /api/admin/alerts")
    class GetAlerts {

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("전체 알림 목록 조회 성공")
        void getAlerts_success() throws Exception {
            // Arrange
            List<AlertResponse> alerts = List.of(
                    new AlertResponse("alert-1", 1L, "홍길동", "WARNING",
                            "안부 체크 미응답 감지", "ACTIVE", LocalDateTime.now(), null),
                    new AlertResponse("alert-2", 2L, "김철수", "DANGER",
                            "안부 체크 미응답 감지 (2회)", "ACTIVE", LocalDateTime.now(), null)
            );
            Page<AlertResponse> page = new PageImpl<>(alerts, PageRequest.of(0, 20), 2);
            given(adminAlertService.getAlerts(any(), any())).willReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/admin/alerts"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.content[0].level").value("WARNING"))
                    .andExpect(jsonPath("$.data.content[0].status").value("ACTIVE"));
        }

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("상태 필터링으로 알림 조회 성공")
        void getAlerts_withStatusFilter() throws Exception {
            // Arrange
            List<AlertResponse> alerts = List.of(
                    new AlertResponse("alert-1", 1L, "홍길동", "WARNING",
                            "미응답 감지", "ACTIVE", LocalDateTime.now(), null)
            );
            Page<AlertResponse> page = new PageImpl<>(alerts, PageRequest.of(0, 20), 1);
            given(adminAlertService.getAlerts(eq("ACTIVE"), any())).willReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/admin/alerts")
                            .param("status", "ACTIVE"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1));
        }

        @Test
        @DisplayName("인증 없이 알림 조회 시 401 응답")
        void getAlerts_unauthorized() throws Exception {
            mockMvc.perform(get("/api/admin/alerts"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/alerts/{alertId}/resolve")
    class ResolveAlert {

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("알림 해결 성공")
        void resolveAlert_success() throws Exception {
            // Arrange
            willDoNothing().given(adminAlertService).resolveAlert("alert-1", 1L);

            // Act & Assert
            mockMvc.perform(patch("/api/admin/alerts/alert-1/resolve"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("알림 종료 완료"));
        }

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("존재하지 않는 알림 해결 시 404 응답")
        void resolveAlert_notFound() throws Exception {
            // Arrange
            willThrow(new ResourceNotFoundException("알림을 찾을 수 없습니다: nonexistent"))
                    .given(adminAlertService).resolveAlert("nonexistent", 1L);

            // Act & Assert
            mockMvc.perform(patch("/api/admin/alerts/nonexistent/resolve"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
