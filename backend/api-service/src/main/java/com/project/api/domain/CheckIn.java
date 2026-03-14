package com.project.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "checkins")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckIn extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CheckInStatus status;

    @Column(length = 200)
    private String message;

    @Column(nullable = false)
    private LocalDateTime checkedAt;

    @Column(name = "mood_score")
    private Integer moodScore;

    public enum CheckInStatus {
        CHECKED, MISSED
    }

    @Builder
    public CheckIn(User user, CheckInStatus status, String message, Integer moodScore) {
        this.user = user;
        this.status = status;
        this.message = message;
        this.moodScore = moodScore;
        this.checkedAt = LocalDateTime.now();
    }
}
