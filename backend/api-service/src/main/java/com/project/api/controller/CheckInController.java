package com.project.api.controller;

import com.project.api.dto.checkin.CheckInRequest;
import com.project.api.dto.checkin.CheckInResponse;
import com.project.api.service.CheckInService;
import com.project.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public ResponseEntity<ApiResponse<CheckInResponse>> checkIn(
            Authentication authentication,
            @RequestBody(required = false) CheckInRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        CheckInRequest effectiveRequest = request != null ? request : new CheckInRequest();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(checkInService.checkIn(userId, effectiveRequest), "안부 체크 완료"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CheckInResponse>>> getMyCheckIns(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(checkInService.getMyCheckIns(userId, pageable)));
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<CheckInResponse>> getLatestCheckIn(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(checkInService.getLatestCheckIn(userId)));
    }
}
