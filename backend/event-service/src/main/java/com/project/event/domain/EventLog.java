package com.project.event.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "event_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLog {

    @Id
    private String id;

    private String eventId;

    private String eventType;

    private String source;

    private String aggregateId;

    private String payload;

    private String status; // RECEIVED, PROCESSING, PROCESSED, FAILED

    private LocalDateTime occurredAt;

    private LocalDateTime processedAt;

    private String errorMessage;

    @CreatedDate
    private LocalDateTime createdAt;
}
