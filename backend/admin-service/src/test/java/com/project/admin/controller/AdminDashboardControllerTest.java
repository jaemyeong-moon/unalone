package com.project.admin.controller;

import com.project.admin.dto.DashboardResponse;
import com.project.admin.service.AdminDashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminDashboardController.class)
@DisplayName("AdminDashboardController 테스트")
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminDashboardService dashboardService;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("인증된 관리자로 대시보드 조회 성공")
    void getDashboard_success() throws Exception {
        // Arrange
        DashboardResponse response = new DashboardResponse(
                100L, 80L, 50L, 5L, 3L, 1L, 1L,
                2L, 30L, 10L, 3L, 5L, 7.5, 2L);
        given(dashboardService.getDashboard()).willReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/admin/dashboard"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUsers").value(100))
                .andExpect(jsonPath("$.data.activeUsers").value(80))
                .andExpect(jsonPath("$.data.todayCheckIns").value(50))
                .andExpect(jsonPath("$.data.activeAlerts").value(5))
                .andExpect(jsonPath("$.data.activeEscalations").value(2))
                .andExpect(jsonPath("$.data.activeVolunteers").value(10))
                .andExpect(jsonPath("$.data.avgHealthScore").value(7.5));
    }

    @Test
    @DisplayName("인증 없이 대시보드 접근 시 401 응답")
    void getDashboard_unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/admin/dashboard"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
