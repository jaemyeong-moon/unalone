package com.project.admin.controller;

import com.project.admin.dto.VolunteerAdminResponse;
import com.project.admin.service.AdminVolunteerService;
import com.project.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/volunteers")
@RequiredArgsConstructor
public class AdminVolunteerController {

    private final AdminVolunteerService adminVolunteerService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VolunteerAdminResponse>>> getVolunteers(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminVolunteerService.getVolunteers(status, pageable), "자원봉사자 목록 조회 성공"));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveVolunteer(@PathVariable Long id) {
        adminVolunteerService.approveVolunteer(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "자원봉사자 승인 완료"));
    }
}
