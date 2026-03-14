package com.project.api.domain;

import com.project.api.domain.enums.CareMatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "care_matches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareMatch extends BaseEntity {

    @Column(name = "volunteer_id", nullable = false)
    private Long volunteerId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CareMatchStatus status;

    @Column
    private LocalDateTime matchedAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private Double distance;

    @Builder
    public CareMatch(Long volunteerId, Long receiverId, Double distance) {
        this.volunteerId = volunteerId;
        this.receiverId = receiverId;
        this.distance = distance;
        this.status = CareMatchStatus.PENDING;
        this.matchedAt = LocalDateTime.now();
    }

    public void accept() {
        this.status = CareMatchStatus.ACTIVE;
    }

    public void complete() {
        this.status = CareMatchStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = CareMatchStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }
}
