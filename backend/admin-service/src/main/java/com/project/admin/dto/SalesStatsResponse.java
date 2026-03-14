package com.project.admin.dto;

import java.math.BigDecimal;

/**
 * 매출 통계 응답 DTO
 */
public record SalesStatsResponse(
        BigDecimal todaySales,
        long todayOrderCount,
        BigDecimal monthlySales,
        long monthlyOrderCount,
        long totalUsers,
        long totalProducts
) {
}
