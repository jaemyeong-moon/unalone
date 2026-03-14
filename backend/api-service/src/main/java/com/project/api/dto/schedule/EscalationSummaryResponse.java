package com.project.api.dto.schedule;

public record EscalationSummaryResponse(
        long reminderCount,
        long warningCount,
        long dangerCount,
        long criticalCount,
        long totalActiveCount
) {
    public static EscalationSummaryResponse of(long reminderCount, long warningCount,
                                                long dangerCount, long criticalCount) {
        return new EscalationSummaryResponse(
                reminderCount,
                warningCount,
                dangerCount,
                criticalCount,
                reminderCount + warningCount + dangerCount + criticalCount
        );
    }
}
