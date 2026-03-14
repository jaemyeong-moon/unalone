package com.project.admin.domain;

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

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 2000)
    private String reportContent;

    @Column(name = "receiver_condition", length = 20)
    private String receiverCondition;

    @Column(length = 1000)
    private String specialNotes;

    @Column
    private LocalDateTime visitedAt;
}
