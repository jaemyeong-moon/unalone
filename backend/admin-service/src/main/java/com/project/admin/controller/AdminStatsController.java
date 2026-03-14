package com.project.admin.controller;

import com.project.admin.dto.SalesStatsResponse;
import com.project.admin.service.AdminStatsService;
import com.project.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<SalesStatsResponse>> getSalesStats() {
        SalesStatsResponse stats = adminStatsService.getSalesStats();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
