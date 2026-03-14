package com.project.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_quality_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostQualityLog extends BaseEntity {

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QualityGrade grade;

    @Column(columnDefinition = "TEXT")
    private String scoringDetails;

    @Builder
    public PostQualityLog(Long postId, int score, QualityGrade grade, String scoringDetails) {
        this.postId = postId;
        this.score = score;
        this.grade = grade;
        this.scoringDetails = scoringDetails;
    }
}
