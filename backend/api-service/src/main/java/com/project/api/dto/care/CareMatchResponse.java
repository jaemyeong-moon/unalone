package com.project.api.dto.care;

import com.project.api.domain.CareMatch;

import java.time.LocalDateTime;

public record CareMatchResponse(
        Long id,
        Long volunteerId,
        Long receiverId,
        String status,
        LocalDateTime matchedAt,
        LocalDateTime completedAt,
        Double distance
) {
    public static CareMatchResponse from(CareMatch careMatch) {
        return new CareMatchResponse(
                careMatch.getId(),
                careMatch.getVolunteerId(),
                careMatch.getReceiverId(),
                careMatch.getStatus().name(),
                careMatch.getMatchedAt(),
                careMatch.getCompletedAt(),
                careMatch.getDistance()
        );
    }
}
