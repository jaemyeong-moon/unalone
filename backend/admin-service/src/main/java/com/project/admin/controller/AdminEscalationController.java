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

import java.util.Map;

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

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getEscalationSummary() {
        return ResponseEntity.ok(ApiResponse.ok(
                adminEscalationService.getEscalationSummary(), "에스컬레이션 요약 조회 성공"));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolveEscalation(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        adminEscalationService.resolveEscalation(id, notes);
        return ResponseEntity.ok(ApiResponse.ok(null, "에스컬레이션 해결 완료"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<EscalationAdminResponse>>> getUserEscalations(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                adminEscalationService.getUserEscalations(userId, pageable), "사용자 에스컬레이션 이력 조회 성공"));
    }
}
