package com.project.admin.dto;

import com.project.admin.domain.Alert;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {
    private String id;
    private Long userId;
    private String userName;
    private String level;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public static AlertResponse from(Alert alert, String userName) {
        return AlertResponse.builder()
                .id(alert.getId()).userId(alert.getUserId()).userName(userName)
                .level(alert.getLevel()).message(alert.getMessage())
                .status(alert.getStatus()).createdAt(alert.getCreatedAt())
                .resolvedAt(alert.getResolvedAt()).build();
    }
}
