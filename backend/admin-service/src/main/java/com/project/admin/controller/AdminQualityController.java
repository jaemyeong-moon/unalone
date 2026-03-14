package com.project.admin.controller;

import com.project.admin.dto.QualityOverrideRequest;
import com.project.admin.dto.QualityPostResponse;
import com.project.admin.dto.QualityStatsResponse;
import com.project.admin.service.AdminQualityService;
import com.project.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/quality")
@RequiredArgsConstructor
public class AdminQualityController {

    private final AdminQualityService qualityService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<QualityStatsResponse>> getQualityStats() {
        return ResponseEntity.ok(ApiResponse.ok(qualityService.getQualityStats()));
    }

    @GetMapping("/flagged")
    public ResponseEntity<ApiResponse<Page<QualityPostResponse>>> getFlaggedPosts(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(qualityService.getFlaggedPosts(pageable)));
    }

    @PutMapping("/{postId}/override")
    public ResponseEntity<ApiResponse<Void>> overrideQualityGrade(
            @PathVariable Long postId,
            @Valid @RequestBody QualityOverrideRequest request) {
        qualityService.overrideQualityGrade(postId, request);
        return ResponseEntity.ok(ApiResponse.ok(null, "품질 등급이 수동으로 변경되었습니다"));
    }
}
