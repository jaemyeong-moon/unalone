package com.project.admin.dto;

/**
 * 대시보드 통계 응답 DTO
 */
public record DashboardResponse(
        long totalUsers,
        long activeUsers,
        long todayCheckIns,
        long activeAlerts,
        long warningAlerts,
        long dangerAlerts,
        long criticalAlerts
) {
}
