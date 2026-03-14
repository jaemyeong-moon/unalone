package com.project.admin.dto;

import java.util.Map;

public record QualityStatsResponse(
        double averageScore,
        long totalScoredPosts,
        Map<String, Long> gradeDistribution
) {
}
