package com.project.admin.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalUsers;
    private long activeUsers;
    private long todayCheckIns;
    private long activeAlerts;
    private long warningAlerts;
    private long dangerAlerts;
    private long criticalAlerts;
}
