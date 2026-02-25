package com.project.admin.controller;

import com.project.admin.dto.AlertResponse;
import com.project.admin.service.AdminAlertService;
import com.project.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/alerts")
@RequiredArgsConstructor
public class AdminAlertController {
    private final AdminAlertService adminAlertService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AlertResponse>>> getAlerts(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(adminAlertService.getAlerts(status, pageable)));
    }

    @PatchMapping("/{alertId}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolveAlert(@PathVariable String alertId) {
        adminAlertService.resolveAlert(alertId, 1L); // TODO: get from auth
        return ResponseEntity.ok(ApiResponse.ok(null, "알림 종료 완료"));
    }
}
