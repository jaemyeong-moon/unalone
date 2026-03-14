package com.project.common.event;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString(callSuper = true)
public class AnomalyDetectedEvent extends DomainEvent {

    private final Long userId;
    private final String anomalyId;
    private final String anomalyType;
    private final String severity;
    private final LocalDateTime detectedAt;

    public AnomalyDetectedEvent(Long userId, String anomalyId, String anomalyType, String severity) {
        super("ANOMALY_DETECTED", String.valueOf(userId), "api-service");
        this.userId = userId;
        this.anomalyId = anomalyId;
        this.anomalyType = anomalyType;
        this.severity = severity;
        this.detectedAt = LocalDateTime.now();
    }
}
