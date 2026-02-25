package com.project.event.repository;

import com.project.event.domain.EventLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventLogRepository extends MongoRepository<EventLog, String> {
    Page<EventLog> findByEventType(String eventType, Pageable pageable);
    Page<EventLog> findBySource(String source, Pageable pageable);
}
