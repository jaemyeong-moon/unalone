package com.project.api.dto.quality;

import java.util.Map;

public record QualityStatsResponse(
        double averageScore,
        long totalScoredPosts,
        Map<String, Long> gradeDistribution
) {
}
