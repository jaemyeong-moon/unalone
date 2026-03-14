package com.project.api.dto.article;

import com.project.api.domain.CrawledArticle;

import java.time.LocalDateTime;

public record CrawledArticleResponse(
        Long id,
        String originalTitle,
        String summary,
        String thumbnailUrl,
        String author,
        String category,
        String status,
        int qualityScore,
        long viewCount,
        LocalDateTime publishedAt,
        LocalDateTime crawledAt
) {
    public static CrawledArticleResponse from(CrawledArticle article) {
        return new CrawledArticleResponse(
                article.getId(),
                article.getOriginalTitle(),
                article.getSummary(),
                article.getThumbnailUrl(),
                article.getAuthor(),
                article.getCategory().name(),
                article.getStatus().name(),
                article.getQualityScore(),
                article.getViewCount(),
                article.getPublishedAt(),
                article.getCrawledAt()
        );
    }
}
