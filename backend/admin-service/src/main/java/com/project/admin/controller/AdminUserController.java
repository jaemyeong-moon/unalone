package com.project.admin.controller;

import com.project.admin.dto.UserDetailResponse;
import com.project.admin.service.AdminUserService;
import com.project.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserDetailResponse>>> getUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(adminUserService.getUsers(pageable)));
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long userId, @RequestParam String status) {
        adminUserService.updateUserStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.ok(null, "사용자 상태 변경 완료"));
    }
}
