package com.project.event.controller;

import com.project.common.dto.ApiResponse;
import com.project.event.domain.EventLog;
import com.project.event.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EventLogController {

    private final EventLogRepository eventLogRepository;

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<Page<EventLog>>> getEvents(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(eventLogRepository.findAll(pageable)));
    }

    @GetMapping("/events/type/{eventType}")
    public ResponseEntity<ApiResponse<Page<EventLog>>> getEventsByType(
            @PathVariable String eventType,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(eventLogRepository.findByEventType(eventType, pageable)));
    }

    @GetMapping("/events/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.ok("Event Service is running"));
    }
}
