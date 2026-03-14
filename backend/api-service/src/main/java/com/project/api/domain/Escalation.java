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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkin_id")
    private CheckIn checkIn;

    @Column(name = "checkin_schedule_id")
    private Long checkInScheduleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EscalationStage stage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EscalationStatus status;

    @Column(nullable = false)
    private LocalDateTime triggeredAt;

    @Column
    private LocalDateTime resolvedAt;

    @Column(nullable = false)
    private boolean resolved;

    @Column(length = 50)
    private String resolvedBy;

    @Column(nullable = false)
    private int notificationsSent;

    @Column(length = 1000)
    private String notes;

    @Column(length = 500)
    private String notifiedContacts;

    /**
     * 에스컬레이션 상태 enum.
     */
    public enum EscalationStatus {
        ACTIVE, RESOLVED, CANCELLED
    }

    @Builder
    public Escalation(User user, CheckIn checkIn, Long checkInScheduleId, EscalationStage stage,
                      LocalDateTime triggeredAt, String notifiedContacts) {
        this.user = user;
        this.checkIn = checkIn;
        this.checkInScheduleId = checkInScheduleId;
        this.stage = stage != null ? stage : EscalationStage.REMINDER;
        this.status = EscalationStatus.ACTIVE;
        this.triggeredAt = triggeredAt != null ? triggeredAt : LocalDateTime.now();
        this.resolved = false;
        this.notificationsSent = 0;
        this.notifiedContacts = notifiedContacts;
    }

    public void escalateTo(EscalationStage newStage) {
        this.stage = newStage;
    }

    public void resolve() {
        this.resolved = true;
        this.resolvedAt = LocalDateTime.now();
        this.status = EscalationStatus.RESOLVED;
        this.resolvedBy = "USER_CHECKIN";
    }

    public void resolveByAdmin(String adminIdentifier, String notes) {
        this.resolved = true;
        this.resolvedAt = LocalDateTime.now();
        this.status = EscalationStatus.RESOLVED;
        this.resolvedBy = "ADMIN:" + adminIdentifier;
        this.notes = notes;
    }

    public void cancel() {
        this.resolved = true;
        this.resolvedAt = LocalDateTime.now();
        this.status = EscalationStatus.CANCELLED;
        this.resolvedBy = "AUTO";
    }

    public void incrementNotificationsSent() {
        this.notificationsSent++;
    }

    public void updateNotifiedContacts(String contacts) {
        this.notifiedContacts = contacts;
    }

    public void addNote(String note) {
        if (this.notes == null || this.notes.isBlank()) {
            this.notes = note;
        } else {
            this.notes = this.notes + "; " + note;
        }
    }
}
