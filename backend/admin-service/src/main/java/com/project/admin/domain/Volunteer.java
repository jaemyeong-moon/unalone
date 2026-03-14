package com.project.admin.domain;

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

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private Integer trustScore;

    @Column(nullable = false)
    private Integer totalVisits;

    @Column
    private LocalDateTime approvedAt;

    public void approve() {
        this.status = "APPROVED";
        this.approvedAt = LocalDateTime.now();
    }
}
