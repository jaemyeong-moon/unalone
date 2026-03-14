package com.project.admin.domain;

public enum QualityGrade {
    EXCELLENT,  // 90-100
    GOOD,       // 70-89
    NORMAL,     // 50-69
    LOW,        // 30-49
    SPAM;       // 0-29

    public static QualityGrade fromScore(int score) {
        if (score >= 90) return EXCELLENT;
        if (score >= 70) return GOOD;
        if (score >= 50) return NORMAL;
        if (score >= 30) return LOW;
        return SPAM;
    }
}
