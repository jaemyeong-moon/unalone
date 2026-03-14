package com.project.admin.domain;

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

    @Column(nullable = false, length = 20)
    private String status;

    @Column
    private LocalDateTime matchedAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private Double distance;
}
