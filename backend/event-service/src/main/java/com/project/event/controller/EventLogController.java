package com.project.event.controller;

import com.project.common.dto.ApiResponse;
import com.project.event.domain.EventLog;
import com.project.event.service.EventLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이벤트 로그 조회 REST 컨트롤러.
 */
@RestController
@RequiredArgsConstructor
public class EventLogController {

    private final EventLogService eventLogService;

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<Page<EventLog>>> getEvents(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(eventLogService.findAll(pageable)));
    }

    @GetMapping("/events/type/{eventType}")
    public ResponseEntity<ApiResponse<Page<EventLog>>> getEventsByType(
            @PathVariable String eventType,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(eventLogService.findByEventType(eventType, pageable)));
    }

    @GetMapping("/events/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.ok("Event Service is running"));
    }
}
