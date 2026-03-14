package com.project.api.controller;

import com.project.api.dto.care.CareVisitReportRequest;
import com.project.api.dto.care.CareVisitRequest;
import com.project.api.dto.care.CareVisitResponse;
import com.project.api.service.CareVisitService;
import com.project.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/care/visits")
@RequiredArgsConstructor
public class CareVisitController {

    private final CareVisitService careVisitService;

    @PostMapping
    public ResponseEntity<ApiResponse<CareVisitResponse>> scheduleVisit(
            Authentication authentication,
            @Valid @RequestBody CareVisitRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(careVisitService.scheduleVisit(userId, request), "방문 예약 완료"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CareVisitResponse>>> getVisits(
            Authentication authentication,
            @RequestParam(required = false) Long matchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                careVisitService.getVisits(userId, matchId, startDate, endDate), "방문 목록 조회 성공"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CareVisitResponse>> getVisitDetail(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(careVisitService.getVisitDetail(userId, id), "방문 상세 조회 성공"));
    }

    @PutMapping("/{id}/report")
    public ResponseEntity<ApiResponse<CareVisitResponse>> submitReport(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody CareVisitReportRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(careVisitService.submitReport(userId, id, request), "방문 보고서 제출 완료"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<CareVisitResponse>> cancelVisit(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(careVisitService.cancelVisit(userId, id), "방문 취소 완료"));
    }
}
