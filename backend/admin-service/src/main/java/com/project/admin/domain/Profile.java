package com.project.admin.domain;

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
}
