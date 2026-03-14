package com.project.api.controller;

import com.project.api.domain.EscalationStage;
import com.project.api.dto.schedule.EscalationResolveRequest;
import com.project.api.dto.schedule.EscalationResponse;
import com.project.api.dto.schedule.EscalationSummaryResponse;
import com.project.api.service.EscalationService;
import com.project.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/escalations")
@RequiredArgsConstructor
public class EscalationController {

    private final EscalationService escalationService;

    /**
     * 활성 에스컬레이션 목록을 조회합니다 (관리자용).
     * 선택적으로 stage 파라미터로 필터링할 수 있습니다.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Page<EscalationResponse>>> getActiveEscalations(
            @RequestParam(required = false) String stage,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<EscalationResponse> result;
        if (stage != null && !stage.isBlank()) {
            EscalationStage escalationStage = EscalationStage.valueOf(stage.toUpperCase());
            result = escalationService.getActiveEscalationsByStage(escalationStage, pageable);
        } else {
            result = escalationService.getActiveEscalations(pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(result, "활성 에스컬레이션 조회 성공"));
    }

    /**
     * 에스컬레이션 요약 정보를 반환합니다 (관리자 대시보드용).
     */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<EscalationSummaryResponse>> getEscalationSummary() {
        return ResponseEntity.ok(ApiResponse.ok(
                escalationService.getEscalationSummary(),
                "에스컬레이션 요약 조회 성공"));
    }

    /**
     * 에스컬레이션을 수동 해제합니다 (관리자용).
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<EscalationResponse>> resolveEscalation(
            @PathVariable Long id,
            @RequestBody(required = false) EscalationResolveRequest request,
            Authentication authentication) {
        String adminIdentifier = authentication != null
                ? authentication.getPrincipal().toString()
                : "ADMIN";
        String notes = request != null ? request.notes() : null;

        return ResponseEntity.ok(ApiResponse.ok(
                escalationService.resolveByAdmin(id, adminIdentifier, notes),
                "에스컬레이션이 해제되었습니다"));
    }

    /**
     * 사용자별 에스컬레이션 이력을 조회합니다 (관리자용).
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<EscalationResponse>>> getUserEscalationHistory(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                escalationService.getUserEscalationHistory(userId, pageable),
                "사용자 에스컬레이션 이력 조회 성공"));
    }
}
