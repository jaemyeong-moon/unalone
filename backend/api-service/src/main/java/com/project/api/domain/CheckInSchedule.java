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
    private LocalTime quietStartTime;

    @Column
    private LocalTime quietEndTime;

    @Column(name = "is_paused", nullable = false)
    private boolean paused;

    @Column(length = 200)
    private String pauseReason;

    @Column
    private LocalDate pauseEndDate;

    @Column
    private LocalDate pauseUntil;

    @Column(nullable = false)
    private boolean enabled;

    @Column
    private LocalDateTime nextCheckInDue;

    @Builder
    public CheckInSchedule(User user, Integer intervalHours, LocalTime preferredTime,
                           String activeDays, LocalTime quietStartTime, LocalTime quietEndTime,
                           LocalDate pauseUntil) {
        this.user = user;
        this.intervalHours = intervalHours != null ? intervalHours : 24;
        this.preferredTime = preferredTime != null ? preferredTime : LocalTime.of(9, 0);
        this.activeDays = activeDays != null ? activeDays : "MON,TUE,WED,THU,FRI,SAT,SUN";
        this.quietStartTime = quietStartTime != null ? quietStartTime : LocalTime.of(22, 0);
        this.quietEndTime = quietEndTime != null ? quietEndTime : LocalTime.of(8, 0);
        this.paused = false;
        this.enabled = true;
        this.pauseUntil = pauseUntil;
    }

    public void updateSchedule(Integer intervalHours, LocalTime preferredTime, String activeDays) {
        if (intervalHours != null) this.intervalHours = intervalHours;
        if (preferredTime != null) this.preferredTime = preferredTime;
        if (activeDays != null) this.activeDays = activeDays;
    }

    public void updateQuietHours(LocalTime quietStartTime, LocalTime quietEndTime) {
        if (quietStartTime != null) this.quietStartTime = quietStartTime;
        if (quietEndTime != null) this.quietEndTime = quietEndTime;
    }

    public void pause(String reason, LocalDate endDate) {
        this.paused = true;
        this.pauseReason = reason;
        this.pauseEndDate = endDate;
        this.pauseUntil = endDate;
    }

    public void pause(LocalDate pauseUntil) {
        this.paused = true;
        this.pauseUntil = pauseUntil;
        this.pauseEndDate = pauseUntil;
    }

    public void resume() {
        this.paused = false;
        this.pauseReason = null;
        this.pauseEndDate = null;
        this.pauseUntil = null;
    }

    public void updateNextCheckInDue(LocalDateTime nextDue) {
        this.nextCheckInDue = nextDue;
    }

    public boolean isPaused() {
        // Auto-resume if pause end date has passed
        if (paused && pauseEndDate != null && LocalDate.now().isAfter(pauseEndDate)) {
            return false;
        }
        if (pauseUntil != null && !LocalDate.now().isAfter(pauseUntil)) {
            return true;
        }
        return paused;
    }

    public boolean isActiveOnDay(String dayOfWeek) {
        return activeDays != null && activeDays.contains(dayOfWeek);
    }

    /**
     * 현재 시간이 야간 알림 제한 시간대(quiet hours)인지 확인합니다.
     */
    public boolean isInQuietHours() {
        if (quietStartTime == null || quietEndTime == null) {
            return false;
        }
        LocalTime now = LocalTime.now();
        // quiet hours가 자정을 걸쳐 있는 경우 (예: 22:00 ~ 08:00)
        if (quietStartTime.isAfter(quietEndTime)) {
            return now.isAfter(quietStartTime) || now.isBefore(quietEndTime);
        }
        return now.isAfter(quietStartTime) && now.isBefore(quietEndTime);
    }

    public LocalDate getPauseUntil() {
        return this.pauseUntil;
    }
}
