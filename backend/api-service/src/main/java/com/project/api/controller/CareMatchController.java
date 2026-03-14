package com.project.api.controller;

import com.project.api.dto.care.CareMatchRequest;
import com.project.api.dto.care.CareMatchResponse;
import com.project.api.service.CareMatchService;
import com.project.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/care/matches")
@RequiredArgsConstructor
public class CareMatchController {

    private final CareMatchService careMatchService;

    @PostMapping
    public ResponseEntity<ApiResponse<CareMatchResponse>> createMatch(
            Authentication authentication,
            @Valid @RequestBody CareMatchRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(careMatchService.createMatch(userId, request.receiverId()), "매칭 생성 완료"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CareMatchResponse>>> getMyMatches(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(careMatchService.getMyMatches(userId), "매칭 목록 조회 성공"));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<CareMatchResponse>> acceptMatch(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(careMatchService.acceptMatch(userId, id), "매칭 수락 완료"));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<CareMatchResponse>> completeMatch(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(careMatchService.completeMatch(userId, id), "매칭 완료 처리 완료"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<CareMatchResponse>> cancelMatch(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(careMatchService.cancelMatch(userId, id), "매칭 취소 완료"));
    }
}
