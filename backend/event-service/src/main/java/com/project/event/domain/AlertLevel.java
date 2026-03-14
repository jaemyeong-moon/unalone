package com.project.event.domain;

/**
 * 알림 심각도 레벨.
 * 체크인 미응답 횟수에 따라 결정됩니다.
 */
public enum AlertLevel {
    /** 1회 미응답 */
    WARNING,
    /** 2회 미응답 */
    DANGER,
    /** 3회 이상 미응답 */
    CRITICAL;

    /**
     * 미응답 횟수를 기반으로 알림 레벨을 결정합니다.
     *
     * @param missedCount 누적 미응답 횟수
     * @return 해당하는 AlertLevel
     */
    public static AlertLevel from(int missedCount) {
        if (missedCount >= 3) return CRITICAL;
        if (missedCount >= 2) return DANGER;
        return WARNING;
    }
}
