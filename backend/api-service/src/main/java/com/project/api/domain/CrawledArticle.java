package com.project.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "crawled_articles", uniqueConstraints = {
        @UniqueConstraint(columnNames = "originalUrl")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrawledArticle extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_source_id", nullable = false)
    private NewsSource newsSource;

    @Column(nullable = false, unique = true)
    private String originalUrl;

    @Column(nullable = false)
    private String originalTitle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String originalContent;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private String thumbnailUrl;

    private String author;

    private LocalDateTime publishedAt;

    @Column(nullable = false)
    private LocalDateTime crawledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleStatus status;

    @Column(nullable = false)
    private int qualityScore;

    @Column(nullable = false)
    private long viewCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleCategory category;

    public enum ArticleStatus {
        CRAWLED, SUMMARIZED, PUBLISHED, REJECTED, FAILED
    }

    @Builder
    public CrawledArticle(NewsSource newsSource, String originalUrl, String originalTitle,
                          String originalContent, String thumbnailUrl, String author,
                          LocalDateTime publishedAt, ArticleCategory category) {
        this.newsSource = newsSource;
        this.originalUrl = originalUrl;
        this.originalTitle = originalTitle;
        this.originalContent = originalContent;
        this.thumbnailUrl = thumbnailUrl;
        this.author = author;
        this.publishedAt = publishedAt;
        this.crawledAt = LocalDateTime.now();
        this.status = ArticleStatus.CRAWLED;
        this.qualityScore = 0;
        this.viewCount = 0;
        this.category = category != null ? category : ArticleCategory.LIFESTYLE;
    }

    public void updateSummary(String summary, int qualityScore) {
        this.summary = summary;
        this.qualityScore = qualityScore;
        this.status = qualityScore >= 60 ? ArticleStatus.SUMMARIZED : ArticleStatus.REJECTED;
    }

    public void publish() {
        this.status = ArticleStatus.PUBLISHED;
    }

    public void reject() {
        this.status = ArticleStatus.REJECTED;
    }

    public void fail() {
        this.status = ArticleStatus.FAILED;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}
