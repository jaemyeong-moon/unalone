package com.project.api.domain;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EscalationStage stage;

    @Column(nullable = false)
    private LocalDateTime triggeredAt;

    @Column
    private LocalDateTime resolvedAt;

    @Column(nullable = false)
    private boolean resolved;

    @Column(length = 500)
    private String notifiedContacts;

    @Builder
    public Escalation(User user, Long checkInScheduleId, EscalationStage stage,
                      LocalDateTime triggeredAt, String notifiedContacts) {
        this.user = user;
        this.checkInScheduleId = checkInScheduleId;
        this.stage = stage != null ? stage : EscalationStage.REMINDER;
        this.triggeredAt = triggeredAt != null ? triggeredAt : LocalDateTime.now();
        this.resolved = false;
        this.notifiedContacts = notifiedContacts;
    }

    public void escalateTo(EscalationStage newStage) {
        this.stage = newStage;
    }

    public void resolve() {
        this.resolved = true;
        this.resolvedAt = LocalDateTime.now();
    }

    public void updateNotifiedContacts(String contacts) {
        this.notifiedContacts = contacts;
    }
}
