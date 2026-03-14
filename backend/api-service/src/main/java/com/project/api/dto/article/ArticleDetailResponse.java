package com.project.api.dto.article;

import com.project.api.domain.CrawledArticle;

import java.time.LocalDateTime;

public record ArticleDetailResponse(
        Long id,
        String originalTitle,
        String originalContent,
        String summary,
        String originalUrl,
        String thumbnailUrl,
        String author,
        String category,
        String status,
        int qualityScore,
        long viewCount,
        String sourceName,
        LocalDateTime publishedAt,
        LocalDateTime crawledAt
) {
    public static ArticleDetailResponse from(CrawledArticle article) {
        return new ArticleDetailResponse(
                article.getId(),
                article.getOriginalTitle(),
                article.getOriginalContent(),
                article.getSummary(),
                article.getOriginalUrl(),
                article.getThumbnailUrl(),
                article.getAuthor(),
                article.getCategory().name(),
                article.getStatus().name(),
                article.getQualityScore(),
                article.getViewCount(),
                article.getNewsSource().getName(),
                article.getPublishedAt(),
                article.getCrawledAt()
        );
    }
}
