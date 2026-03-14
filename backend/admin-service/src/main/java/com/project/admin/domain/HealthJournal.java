package com.project.admin.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "health_journals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthJournal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "journal_date", nullable = false)
    private LocalDate date;

    @Column(name = "mood_score")
    private Integer moodScore;

    @Column(name = "sleep_hours")
    private BigDecimal sleepHours;

    @Column(name = "sleep_quality")
    private Integer sleepQuality;

    @Column(name = "meal_count")
    private Integer mealCount;

    @Column(name = "meal_quality")
    private Integer mealQuality;

    @Column(name = "exercise_minutes")
    private Integer exerciseMinutes;

    @Column(name = "exercise_type", length = 50)
    private String exerciseType;

    @Column(name = "symptoms", length = 500)
    private String symptoms;

    @Column(name = "medications", length = 500)
    private String medications;

    @Column(name = "pain_level")
    private Integer painLevel;

    @Column(length = 500)
    private String notes;

    @Column(name = "health_score")
    private Integer healthScore;
}
