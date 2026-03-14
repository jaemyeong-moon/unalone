package com.project.api.dto.quality;

import com.project.api.domain.PostQualityLog;

import java.time.LocalDateTime;
import java.util.Map;

public record QualityDetailResponse(
        Long postId,
        int score,
        String grade,
        Map<String, Integer> breakdown,
        LocalDateTime scoredAt
) {
    public static QualityDetailResponse from(PostQualityLog log, Map<String, Integer> breakdown) {
        return new QualityDetailResponse(
                log.getPostId(),
                log.getScore(),
                log.getGrade().name(),
                breakdown,
                log.getCreatedAt()
        );
    }
}
