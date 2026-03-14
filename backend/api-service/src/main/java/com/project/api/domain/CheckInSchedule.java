package com.project.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "checkin_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckInSchedule extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer intervalHours;

    @Column(nullable = false)
    private LocalTime preferredTime;

    @Column(nullable = false, length = 100)
    private String activeDays;

    @Column
    private LocalDate pauseUntil;

    @Column
    private LocalDateTime nextCheckInDue;

    @Builder
    public CheckInSchedule(User user, Integer intervalHours, LocalTime preferredTime,
                           String activeDays, LocalDate pauseUntil) {
        this.user = user;
        this.intervalHours = intervalHours != null ? intervalHours : 24;
        this.preferredTime = preferredTime != null ? preferredTime : LocalTime.of(9, 0);
        this.activeDays = activeDays != null ? activeDays : "MON,TUE,WED,THU,FRI,SAT,SUN";
        this.pauseUntil = pauseUntil;
    }

    public void updateSchedule(Integer intervalHours, LocalTime preferredTime, String activeDays) {
        if (intervalHours != null) this.intervalHours = intervalHours;
        if (preferredTime != null) this.preferredTime = preferredTime;
        if (activeDays != null) this.activeDays = activeDays;
    }

    public void pause(LocalDate pauseUntil) {
        this.pauseUntil = pauseUntil;
    }

    public void resume() {
        this.pauseUntil = null;
    }

    public void updateNextCheckInDue(LocalDateTime nextDue) {
        this.nextCheckInDue = nextDue;
    }

    public boolean isPaused() {
        return pauseUntil != null && !LocalDate.now().isAfter(pauseUntil);
    }

    public boolean isActiveOnDay(String dayOfWeek) {
        return activeDays != null && activeDays.contains(dayOfWeek);
    }
}
