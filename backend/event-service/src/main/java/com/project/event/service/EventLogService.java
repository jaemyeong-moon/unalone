package com.project.event.service;

import com.project.event.domain.EventLog;
import com.project.event.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 이벤트 로그 조회 서비스.
 * 컨트롤러와 리포지토리 사이의 서비스 레이어를 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class EventLogService {

    private final EventLogRepository eventLogRepository;

    public Page<EventLog> findAll(Pageable pageable) {
        return eventLogRepository.findAll(pageable);
    }

    public Page<EventLog> findByEventType(String eventType, Pageable pageable) {
        return eventLogRepository.findByEventType(eventType, pageable);
    }
}
