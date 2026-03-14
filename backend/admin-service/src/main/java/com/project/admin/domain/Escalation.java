package com.project.admin.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "escalations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Escalation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "checkin_schedule_id")
    private Long checkInScheduleId;

    @Column(nullable = false, length = 20)
    private String stage;

    @Column(nullable = false)
    private LocalDateTime triggeredAt;

    @Column
    private LocalDateTime resolvedAt;

    @Column(nullable = false)
    private boolean resolved;

    @Column(length = 500)
    private String notifiedContacts;

    public void resolve() {
        this.resolved = true;
        this.resolvedAt = LocalDateTime.now();
    }
}
