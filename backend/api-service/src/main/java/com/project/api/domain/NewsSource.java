package com.project.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "news_sources")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewsSource extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String baseUrl;

    @Column(nullable = false)
    private String crawlPattern;

    @Column(nullable = false)
    private String articlePattern;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleCategory category;

    @Column(nullable = false)
    private boolean enabled;

    private LocalDateTime lastCrawledAt;

    @Builder
    public NewsSource(String name, String baseUrl, String crawlPattern, String articlePattern,
                      ArticleCategory category, boolean enabled) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.crawlPattern = crawlPattern;
        this.articlePattern = articlePattern;
        this.category = category;
        this.enabled = enabled;
    }

    public void update(String name, String baseUrl, String crawlPattern, String articlePattern,
                       ArticleCategory category, Boolean enabled) {
        if (name != null) this.name = name;
        if (baseUrl != null) this.baseUrl = baseUrl;
        if (crawlPattern != null) this.crawlPattern = crawlPattern;
        if (articlePattern != null) this.articlePattern = articlePattern;
        if (category != null) this.category = category;
        if (enabled != null) this.enabled = enabled;
    }

    public void updateLastCrawledAt() {
        this.lastCrawledAt = LocalDateTime.now();
    }
}
