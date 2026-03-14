package com.project.api.domain;

public enum EscalationStage {
    REMINDER,
    WARNING,
    DANGER,
    CRITICAL;

    /**
     * 다음 단계로 진행합니다. CRITICAL이면 CRITICAL을 반환합니다.
     */
    public EscalationStage next() {
        return switch (this) {
            case REMINDER -> WARNING;
            case WARNING -> DANGER;
            case DANGER -> CRITICAL;
            case CRITICAL -> CRITICAL;
        };
    }

    /**
     * 미응답 경과 시간(시간)에 따른 에스컬레이션 단계를 반환합니다.
     */
    public static EscalationStage fromElapsedHours(long elapsedHours) {
        if (elapsedHours >= 6) return CRITICAL;
        if (elapsedHours >= 3) return DANGER;
        if (elapsedHours >= 1) return WARNING;
        return REMINDER;
    }
}
