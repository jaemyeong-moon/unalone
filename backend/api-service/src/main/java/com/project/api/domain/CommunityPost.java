package com.project.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "community_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPost extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostCategory category;

    // === Translation fields ===

    @Column(length = 200)
    private String translatedTitle;

    @Column(columnDefinition = "TEXT")
    private String translatedContent;

    @Column(length = 10)
    private String originalLanguage;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TranslationStatus translationStatus;

    @Column
    private int translationRetryCount;

    // === Quality scoring fields ===

    @Column
    private Integer qualityScore;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private QualityGrade qualityGrade;

    @Column
    private LocalDateTime scoredAt;

    public enum PostCategory {
        DAILY, HEALTH, HOBBY, HELP, NOTICE
    }

    @Builder
    public CommunityPost(User user, String title, String content, PostCategory category) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.category = category != null ? category : PostCategory.DAILY;
        this.translationStatus = TranslationStatus.PENDING;
        this.originalLanguage = "ko";
        this.translationRetryCount = 0;
    }

    public void update(String title, String content, PostCategory category) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (category != null) this.category = category;
        // 게시글 수정 시 번역 및 품질 점수 재계산 필요
        this.translationStatus = TranslationStatus.PENDING;
        this.translationRetryCount = 0;
        this.qualityScore = null;
        this.qualityGrade = null;
        this.scoredAt = null;
    }

    // === Translation methods ===

    public void updateTranslation(String translatedTitle, String translatedContent, String originalLanguage) {
        this.translatedTitle = translatedTitle;
        this.translatedContent = translatedContent;
        this.originalLanguage = originalLanguage;
        this.translationStatus = TranslationStatus.TRANSLATED;
    }

    public void markTranslationFailed() {
        this.translationStatus = TranslationStatus.FAILED;
        this.translationRetryCount++;
    }

    public void markTranslationSkipped(String originalLanguage) {
        this.originalLanguage = originalLanguage;
        this.translationStatus = TranslationStatus.SKIPPED;
    }

    public boolean canRetryTranslation(int maxRetries) {
        return this.translationRetryCount < maxRetries;
    }

    // === Quality scoring methods ===

    public void updateQualityScore(int score, QualityGrade grade) {
        this.qualityScore = score;
        this.qualityGrade = grade;
        this.scoredAt = LocalDateTime.now();
    }
}
