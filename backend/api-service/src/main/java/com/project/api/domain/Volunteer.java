package com.project.api.domain;

import com.project.api.domain.enums.VolunteerStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "volunteers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Volunteer extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "available_days", length = 100)
    private String availableDays;

    @Column(name = "available_time_start", nullable = false)
    private LocalTime availableTimeStart;

    @Column(name = "available_time_end", nullable = false)
    private LocalTime availableTimeEnd;

    @Column(nullable = false)
    private Double radius;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(length = 500)
    private String introduction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VolunteerStatus status;

    @Column(nullable = false)
    private Integer trustScore;

    @Column(nullable = false)
    private Integer totalVisits;

    @Column
    private LocalDateTime approvedAt;

    @Builder
    public Volunteer(Long userId, String availableDays, LocalTime availableTimeStart,
                     LocalTime availableTimeEnd, Double radius, Double latitude,
                     Double longitude, String introduction) {
        this.userId = userId;
        this.availableDays = availableDays;
        this.availableTimeStart = availableTimeStart;
        this.availableTimeEnd = availableTimeEnd;
        this.radius = radius != null ? radius : 2.0;
        this.latitude = latitude;
        this.longitude = longitude;
        this.introduction = introduction;
        this.status = VolunteerStatus.PENDING;
        this.trustScore = 50;
        this.totalVisits = 0;
    }

    public void update(String availableDays, LocalTime availableTimeStart,
                       LocalTime availableTimeEnd, Double radius,
                       Double latitude, Double longitude, String introduction) {
        if (availableDays != null) this.availableDays = availableDays;
        if (availableTimeStart != null) this.availableTimeStart = availableTimeStart;
        if (availableTimeEnd != null) this.availableTimeEnd = availableTimeEnd;
        if (radius != null) this.radius = radius;
        if (latitude != null) this.latitude = latitude;
        if (longitude != null) this.longitude = longitude;
        if (introduction != null) this.introduction = introduction;
    }

    public void approve() {
        this.status = VolunteerStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void suspend() {
        this.status = VolunteerStatus.SUSPENDED;
    }

    public void withdraw() {
        this.status = VolunteerStatus.WITHDRAWN;
    }

    public void addTrustScore(int delta) {
        this.trustScore = Math.max(0, Math.min(100, this.trustScore + delta));
        if (this.trustScore < 30) {
            this.status = VolunteerStatus.SUSPENDED;
        }
    }

    public void incrementTotalVisits() {
        this.totalVisits++;
    }
}
