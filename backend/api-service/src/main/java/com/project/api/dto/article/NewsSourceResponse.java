package com.project.api.dto.article;

import com.project.api.domain.NewsSource;

import java.time.LocalDateTime;

public record NewsSourceResponse(
        Long id,
        String name,
        String baseUrl,
        String crawlPattern,
        String articlePattern,
        String category,
        boolean enabled,
        LocalDateTime lastCrawledAt,
        LocalDateTime createdAt
) {
    public static NewsSourceResponse from(NewsSource source) {
        return new NewsSourceResponse(
                source.getId(),
                source.getName(),
                source.getBaseUrl(),
                source.getCrawlPattern(),
                source.getArticlePattern(),
                source.getCategory().name(),
                source.isEnabled(),
                source.getLastCrawledAt(),
                source.getCreatedAt()
        );
    }
}
