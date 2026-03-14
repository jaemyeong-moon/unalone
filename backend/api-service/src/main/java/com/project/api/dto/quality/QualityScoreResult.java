package com.project.api.dto.quality;

import com.project.api.domain.QualityGrade;

import java.util.Map;

public record QualityScoreResult(
        int score,
        QualityGrade grade,
        Map<String, Integer> breakdown
) {
}
