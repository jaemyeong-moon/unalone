package com.project.api.controller;

import com.project.api.dto.care.VolunteerRequest;
import com.project.api.dto.care.VolunteerResponse;
import com.project.api.service.VolunteerService;
import com.project.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/care/volunteers")
@RequiredArgsConstructor
public class VolunteerController {

    private final VolunteerService volunteerService;

    @PostMapping
    public ResponseEntity<ApiResponse<VolunteerResponse>> register(
            Authentication authentication,
            @Valid @RequestBody VolunteerRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(volunteerService.register(userId, request), "자원봉사자 등록 신청 완료"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<VolunteerResponse>> getMyStatus(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(volunteerService.getMyStatus(userId), "자원봉사자 정보 조회 성공"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<VolunteerResponse>> update(
            Authentication authentication,
            @Valid @RequestBody VolunteerRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(volunteerService.update(userId, request), "자원봉사자 정보 수정 완료"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        volunteerService.withdraw(userId);
        return ResponseEntity.ok(ApiResponse.ok(null, "자원봉사자 탈퇴 완료"));
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<VolunteerResponse>>> findNearby(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false) Double radius) {
        return ResponseEntity.ok(ApiResponse.ok(volunteerService.findNearby(lat, lng, radius), "근처 자원봉사자 조회 성공"));
    }
}
