package com.project.admin.controller;

import com.project.admin.dto.CareVisitAdminResponse;
import com.project.admin.service.AdminCareService;
import com.project.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/care")
@RequiredArgsConstructor
public class AdminCareController {

    private final AdminCareService adminCareService;

    @GetMapping("/visits")
    public ResponseEntity<ApiResponse<Page<CareVisitAdminResponse>>> getVisits(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminCareService.getVisits(pageable), "돌봄 방문 목록 조회 성공"));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<Page<CareVisitAdminResponse>>> getReports(
            @RequestParam(required = false) String condition,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminCareService.getReportsByCondition(condition, pageable), "돌봄 방문 보고서 조회 성공"));
    }
}
