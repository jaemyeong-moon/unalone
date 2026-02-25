package com.project.api.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer checkIntervalHours;

    @Column(length = 10)
    private String activeHoursStart;

    @Column(length = 10)
    private String activeHoursEnd;

    @Column(length = 200)
    private String address;

    @Column(length = 500)
    private String emergencyNote;

    @Builder
    public Profile(User user, Integer checkIntervalHours, String activeHoursStart, String activeHoursEnd,
                   String address, String emergencyNote) {
        this.user = user;
        this.checkIntervalHours = checkIntervalHours != null ? checkIntervalHours : 24;
        this.activeHoursStart = activeHoursStart != null ? activeHoursStart : "08:00";
        this.activeHoursEnd = activeHoursEnd != null ? activeHoursEnd : "22:00";
        this.address = address;
        this.emergencyNote = emergencyNote;
    }

    public void update(Integer checkIntervalHours, String activeHoursStart, String activeHoursEnd,
                       String address, String emergencyNote) {
        if (checkIntervalHours != null) this.checkIntervalHours = checkIntervalHours;
        if (activeHoursStart != null) this.activeHoursStart = activeHoursStart;
        if (activeHoursEnd != null) this.activeHoursEnd = activeHoursEnd;
        if (address != null) this.address = address;
        if (emergencyNote != null) this.emergencyNote = emergencyNote;
    }
}
