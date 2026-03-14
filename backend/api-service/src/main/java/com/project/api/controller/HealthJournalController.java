package com.project.api.controller;

import com.project.api.dto.health.*;
import com.project.api.service.HealthJournalService;
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
@RequestMapping("/api/health/journals")
@RequiredArgsConstructor
public class HealthJournalController {

    private final HealthJournalService healthJournalService;

    @PostMapping
    public ResponseEntity<ApiResponse<HealthJournalResponse>> createJournal(
            Authentication authentication,
            @RequestBody HealthJournalRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        HealthJournalResponse response = healthJournalService.createOrUpdate(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "건강 일지가 저장되었습니다"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<HealthJournalResponse>>> getMyJournals(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(healthJournalService.getMyJournals(userId, pageable), "건강 일지 목록 조회 성공"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HealthJournalResponse>> getJournal(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(healthJournalService.getJournal(userId, id), "건강 일지 상세 조회 성공"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HealthJournalResponse>> updateJournal(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody HealthJournalRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(healthJournalService.updateJournal(userId, id, request), "건강 일지가 수정되었습니다"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJournal(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        healthJournalService.deleteJournal(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "건강 일지가 삭제되었습니다"));
    }

    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<HealthTrendResponse>> getTrends(
            Authentication authentication,
            @RequestParam(defaultValue = "weekly") String period) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(healthJournalService.getTrends(userId, period), "건강 추세 조회 성공"));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<HealthSummaryResponse>> getSummary(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(healthJournalService.getSummary(userId), "건강 요약 조회 성공"));
    }
}
