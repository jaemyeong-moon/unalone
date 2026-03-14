package com.project.event.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Kafka에서 수신한 이벤트의 처리 이력을 저장하는 MongoDB 도큐먼트.
 * 상태 전이는 {@link #markProcessing()}, {@link #markProcessed()}, {@link #markFailed(String)} 메서드로 수행합니다.
 */
@Document(collection = "event_logs")
@Getter
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

    /** 이벤트 처리 상태 */
    private EventLogStatus status;

    private LocalDateTime occurredAt;
    private LocalDateTime processedAt;
    private String errorMessage;

    @CreatedDate
    private LocalDateTime createdAt;

    /** 처리 시작 상태로 전이합니다. */
    public void markProcessing() {
        this.status = EventLogStatus.PROCESSING;
    }

    /** 처리 완료 상태로 전이합니다. */
    public void markProcessed() {
        this.status = EventLogStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 처리 실패 상태로 전이합니다.
     *
     * @param errorMessage 실패 원인 메시지
     */
    public void markFailed(String errorMessage) {
        this.status = EventLogStatus.FAILED;
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }
}
