package com.project.api.domain;

import com.project.api.domain.enums.CareVisitStatus;
import com.project.api.domain.enums.ReceiverCondition;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "care_visits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareVisit extends BaseEntity {

    @Column(name = "care_match_id", nullable = false)
    private Long careMatchId;

    @Column(name = "volunteer_id", nullable = false)
    private Long volunteerId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Column(nullable = false)
    private LocalTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CareVisitStatus status;

    @Column(length = 2000)
    private String reportContent;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReceiverCondition receiverCondition;

    @Column(length = 1000)
    private String specialNotes;

    @Column
    private LocalDateTime visitedAt;

    @Builder
    public CareVisit(Long careMatchId, Long volunteerId, Long receiverId,
                     LocalDate scheduledDate, LocalTime scheduledTime) {
        this.careMatchId = careMatchId;
        this.volunteerId = volunteerId;
        this.receiverId = receiverId;
        this.scheduledDate = scheduledDate;
        this.scheduledTime = scheduledTime;
        this.status = CareVisitStatus.SCHEDULED;
    }

    public void submitReport(String reportContent, ReceiverCondition receiverCondition,
                             String specialNotes) {
        this.status = CareVisitStatus.COMPLETED;
        this.reportContent = reportContent;
        this.receiverCondition = receiverCondition;
        this.specialNotes = specialNotes;
        this.visitedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = CareVisitStatus.CANCELLED;
    }

    public void markNoShow() {
        this.status = CareVisitStatus.NO_SHOW;
    }
}
