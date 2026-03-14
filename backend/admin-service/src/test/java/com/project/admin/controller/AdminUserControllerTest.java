package com.project.admin.controller;

import com.project.admin.dto.UserDetailResponse;
import com.project.admin.exception.InvalidRequestException;
import com.project.admin.exception.ResourceNotFoundException;
import com.project.admin.service.AdminUserService;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@DisplayName("AdminUserController 테스트")
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

    @Nested
    @DisplayName("GET /api/admin/users")
    class GetUsers {

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("사용자 목록 조회 성공")
        void getUsers_success() throws Exception {
            // Arrange
            List<UserDetailResponse> users = List.of(
                    new UserDetailResponse(1L, "test@example.com", "홍길동", "01012345678",
                            "ROLE_USER", "ACTIVE", LocalDateTime.now(), LocalDateTime.now()),
                    new UserDetailResponse(2L, "test2@example.com", "김철수", "01087654321",
                            "ROLE_USER", "ACTIVE", LocalDateTime.now(), null)
            );
            Page<UserDetailResponse> page = new PageImpl<>(users, PageRequest.of(0, 20), 2);
            given(adminUserService.getUsers(any())).willReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/admin/users")
                            .param("page", "0")
                            .param("size", "20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.content[0].name").value("홍길동"));
        }

        @Test
        @DisplayName("인증 없이 사용자 목록 조회 시 401 응답")
        void getUsers_unauthorized() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/users/{userId}/status")
    class UpdateUserStatus {

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("사용자 상태 변경 성공")
        void updateUserStatus_success() throws Exception {
            // Arrange
            willDoNothing().given(adminUserService).updateUserStatus(1L, "SUSPENDED");

            // Act & Assert
            mockMvc.perform(patch("/api/admin/users/1/status")
                            .param("status", "SUSPENDED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("사용자 상태 변경 완료"));
        }

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("존재하지 않는 사용자 상태 변경 시 404 응답")
        void updateUserStatus_userNotFound() throws Exception {
            // Arrange
            willThrow(new ResourceNotFoundException("사용자를 찾을 수 없습니다: 999"))
                    .given(adminUserService).updateUserStatus(999L, "ACTIVE");

            // Act & Assert
            mockMvc.perform(patch("/api/admin/users/999/status")
                            .param("status", "ACTIVE"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @DisplayName("유효하지 않은 상태값으로 변경 시 400 응답")
        void updateUserStatus_invalidStatus() throws Exception {
            // Arrange
            willThrow(new InvalidRequestException("유효하지 않은 사용자 상태: INVALID"))
                    .given(adminUserService).updateUserStatus(1L, "INVALID");

            // Act & Assert
            mockMvc.perform(patch("/api/admin/users/1/status")
                            .param("status", "INVALID"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
