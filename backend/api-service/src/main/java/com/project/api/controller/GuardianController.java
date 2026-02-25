package com.project.api.controller;

import com.project.api.dto.guardian.GuardianRequest;
import com.project.api.dto.guardian.GuardianResponse;
import com.project.api.service.GuardianService;
import com.project.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guardians")
@RequiredArgsConstructor
public class GuardianController {

    private final GuardianService guardianService;

    @PostMapping
    public ResponseEntity<ApiResponse<GuardianResponse>> addGuardian(
            Authentication authentication,
            @Valid @RequestBody GuardianRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(guardianService.addGuardian(userId, request), "보호자 등록 완료"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GuardianResponse>>> getGuardians(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(guardianService.getGuardians(userId)));
    }

    @DeleteMapping("/{guardianId}")
    public ResponseEntity<ApiResponse<Void>> removeGuardian(
            Authentication authentication,
            @PathVariable Long guardianId) {
        Long userId = (Long) authentication.getPrincipal();
        guardianService.removeGuardian(userId, guardianId);
        return ResponseEntity.ok(ApiResponse.ok(null, "보호자 삭제 완료"));
    }
}
