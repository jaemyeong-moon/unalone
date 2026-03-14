package com.project.admin.controller;

import com.project.admin.dto.EscalationAdminResponse;
import com.project.admin.service.AdminEscalationService;
import com.project.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/escalations")
@RequiredArgsConstructor
public class AdminEscalationController {

    private final AdminEscalationService adminEscalationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EscalationAdminResponse>>> getActiveEscalations(
            @RequestParam(required = false) String stage,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminEscalationService.getActiveEscalations(stage, pageable), "에스컬레이션 목록 조회 성공"));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolveEscalation(@PathVariable Long id) {
        adminEscalationService.resolveEscalation(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "에스컬레이션 해결 완료"));
    }
}
