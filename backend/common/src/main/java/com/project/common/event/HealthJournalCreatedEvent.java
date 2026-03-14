package com.project.common.event;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@ToString(callSuper = true)
public class HealthJournalCreatedEvent extends DomainEvent {

    private final Long userId;
    private final LocalDate date;
    private final Integer moodScore;
    private final Integer healthScore;
    private final LocalDateTime recordedAt;

    public HealthJournalCreatedEvent(Long userId, LocalDate date, Integer moodScore, Integer healthScore) {
        super("HEALTH_JOURNAL_CREATED", String.valueOf(userId), "api-service");
        this.userId = userId;
        this.date = date;
        this.moodScore = moodScore;
        this.healthScore = healthScore;
        this.recordedAt = LocalDateTime.now();
    }
}
