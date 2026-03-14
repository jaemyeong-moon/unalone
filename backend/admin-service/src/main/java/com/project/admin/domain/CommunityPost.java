package com.project.admin.domain;

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

    @Column(length = 200)
    private String translatedTitle;

    @Column(columnDefinition = "TEXT")
    private String translatedContent;

    @Column(length = 10)
    private String originalLanguage;

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

    public void updateQualityGrade(QualityGrade grade) {
        this.qualityGrade = grade;
        // 등급 수동 변경 시 점수도 해당 등급 범위 중간값으로 설정
        this.qualityScore = switch (grade) {
            case EXCELLENT -> 95;
            case GOOD -> 80;
            case NORMAL -> 60;
            case LOW -> 40;
            case SPAM -> 15;
        };
        this.scoredAt = LocalDateTime.now();
    }
}
