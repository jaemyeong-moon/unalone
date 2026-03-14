package com.project.common.event;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString(callSuper = true)
public class CareVisitCompletedEvent extends DomainEvent {

    private final Long visitId;
    private final Long matchId;
    private final Long volunteerId;
    private final Long receiverUserId;
    private final String receiverCondition;
    private final String specialNotes;
    private final LocalDateTime visitedAt;

    public CareVisitCompletedEvent(Long visitId, Long matchId, Long volunteerId,
                                   Long receiverUserId, String receiverCondition,
                                   String specialNotes, LocalDateTime visitedAt) {
        super("CARE_VISIT_COMPLETED", String.valueOf(visitId), "api-service");
        this.visitId = visitId;
        this.matchId = matchId;
        this.volunteerId = volunteerId;
        this.receiverUserId = receiverUserId;
        this.receiverCondition = receiverCondition;
        this.specialNotes = specialNotes;
        this.visitedAt = visitedAt;
    }
}
