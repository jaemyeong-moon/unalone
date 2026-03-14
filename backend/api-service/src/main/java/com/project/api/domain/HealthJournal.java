package com.project.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "health_journals", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "journal_date"})
})
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

    @Builder
    public HealthJournal(User user, LocalDate date, Integer moodScore, BigDecimal sleepHours,
                         Integer sleepQuality, Integer mealCount, Integer mealQuality,
                         Integer exerciseMinutes, String exerciseType, String symptoms,
                         String medications, Integer painLevel, String notes, Integer healthScore) {
        this.user = user;
        this.date = date;
        this.moodScore = moodScore;
        this.sleepHours = sleepHours;
        this.sleepQuality = sleepQuality;
        this.mealCount = mealCount;
        this.mealQuality = mealQuality;
        this.exerciseMinutes = exerciseMinutes;
        this.exerciseType = exerciseType;
        this.symptoms = symptoms;
        this.medications = medications;
        this.painLevel = painLevel;
        this.notes = notes;
        this.healthScore = healthScore;
    }

    public void update(Integer moodScore, BigDecimal sleepHours, Integer sleepQuality,
                       Integer mealCount, Integer mealQuality, Integer exerciseMinutes,
                       String exerciseType, String symptoms, String medications,
                       Integer painLevel, String notes, Integer healthScore) {
        if (moodScore != null) this.moodScore = moodScore;
        if (sleepHours != null) this.sleepHours = sleepHours;
        if (sleepQuality != null) this.sleepQuality = sleepQuality;
        if (mealCount != null) this.mealCount = mealCount;
        if (mealQuality != null) this.mealQuality = mealQuality;
        if (exerciseMinutes != null) this.exerciseMinutes = exerciseMinutes;
        if (exerciseType != null) this.exerciseType = exerciseType;
        if (symptoms != null) this.symptoms = symptoms;
        if (medications != null) this.medications = medications;
        if (painLevel != null) this.painLevel = painLevel;
        if (notes != null) this.notes = notes;
        if (healthScore != null) this.healthScore = healthScore;
    }
}
