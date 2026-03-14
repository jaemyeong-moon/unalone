package com.project.api.controller;

import com.project.api.dto.schedule.CheckInScheduleRequest;
import com.project.api.dto.schedule.CheckInScheduleResponse;
import com.project.api.dto.schedule.EscalationResponse;
import com.project.api.service.CheckInScheduleService;
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
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckInScheduleController {

    private final CheckInScheduleService checkInScheduleService;
    private final EscalationService escalationService;

    @GetMapping("/schedule")
    public ResponseEntity<ApiResponse<CheckInScheduleResponse>> getMySchedule(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(checkInScheduleService.getSchedule(userId), "체크인 스케줄 조회 성공"));
    }

    @PutMapping("/schedule")
    public ResponseEntity<ApiResponse<CheckInScheduleResponse>> updateSchedule(
            Authentication authentication,
            @RequestBody CheckInScheduleRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                checkInScheduleService.createOrUpdateSchedule(userId, request),
                "체크인 스케줄이 업데이트되었습니다"));
    }

    @GetMapping("/escalations")
    public ResponseEntity<ApiResponse<Page<EscalationResponse>>> getMyEscalations(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(escalationService.getMyEscalations(userId, pageable), "에스컬레이션 이력 조회 성공"));
    }

    @GetMapping("/escalations/active")
    public ResponseEntity<ApiResponse<EscalationResponse>> getActiveEscalation(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        EscalationResponse active = escalationService.getActiveEscalation(userId);
        if (active == null) {
            return ResponseEntity.ok(ApiResponse.ok(null, "활성 에스컬레이션이 없습니다"));
        }
        return ResponseEntity.ok(ApiResponse.ok(active, "활성 에스컬레이션 조회 성공"));
    }
}
