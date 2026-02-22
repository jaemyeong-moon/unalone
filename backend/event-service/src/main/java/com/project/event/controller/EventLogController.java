package com.project.event.controller;

import com.project.common.response.ApiResponse;
import com.project.event.domain.EventLog;
import com.project.event.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventLogController {

    private final EventLogRepository eventLogRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<EventLog>>> getAllEvents(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<EventLog> events = eventLogRepository.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventLog>> getEventById(@PathVariable String id) {
        return eventLogRepository.findById(id)
                .map(event -> ResponseEntity.ok(ApiResponse.success(event)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{eventType}")
    public ResponseEntity<ApiResponse<List<EventLog>>> getEventsByType(
            @PathVariable String eventType) {
        List<EventLog> events = eventLogRepository.findByEventType(eventType);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/source/{source}")
    public ResponseEntity<ApiResponse<List<EventLog>>> getEventsBySource(
            @PathVariable String source) {
        List<EventLog> events = eventLogRepository.findBySource(source);
        return ResponseEntity.ok(ApiResponse.success(events));
    }
}
