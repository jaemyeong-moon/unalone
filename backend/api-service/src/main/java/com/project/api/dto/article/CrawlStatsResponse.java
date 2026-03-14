package com.project.api.dto.article;

public record CrawlStatsResponse(
        long totalArticles,
        long crawledCount,
        long summarizedCount,
        long publishedCount,
        long rejectedCount,
        long failedCount,
        long crawledLast24h,
        long publishedLast24h
) {
}
