package com.project.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class SalesStatsResponse {
    private BigDecimal todaySales;
    private long todayOrderCount;
    private BigDecimal monthlySales;
    private long monthlyOrderCount;
    private long totalUsers;
    private long totalProducts;
}
