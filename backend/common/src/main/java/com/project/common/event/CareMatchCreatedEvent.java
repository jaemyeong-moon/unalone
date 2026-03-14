package com.project.common.event;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString(callSuper = true)
public class CareMatchCreatedEvent extends DomainEvent {

    private final Long matchId;
    private final Long volunteerId;
    private final Long receiverUserId;
    private final Double distance;
    private final LocalDateTime matchedAt;

    public CareMatchCreatedEvent(Long matchId, Long volunteerId, Long receiverUserId,
                                 Double distance) {
        super("CARE_MATCH_CREATED", String.valueOf(matchId), "api-service");
        this.matchId = matchId;
        this.volunteerId = volunteerId;
        this.receiverUserId = receiverUserId;
        this.distance = distance;
        this.matchedAt = LocalDateTime.now();
    }
}
