package com.project.event.repository;

import com.project.event.domain.EventLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventLogRepository extends MongoRepository<EventLog, String> {

    List<EventLog> findByEventType(String eventType);

    List<EventLog> findBySource(String source);

    List<EventLog> findByStatus(String status);

    List<EventLog> findByOccurredAtBetween(LocalDateTime start, LocalDateTime end);
}
