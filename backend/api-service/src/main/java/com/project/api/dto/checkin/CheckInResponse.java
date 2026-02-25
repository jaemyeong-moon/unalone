package com.project.api.dto.checkin;

import com.project.api.domain.CheckIn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String status;
    private String message;
    private LocalDateTime checkedAt;

    public static CheckInResponse from(CheckIn checkIn) {
        return CheckInResponse.builder()
                .id(checkIn.getId())
                .userId(checkIn.getUser().getId())
                .userName(checkIn.getUser().getName())
                .status(checkIn.getStatus().name())
                .message(checkIn.getMessage())
                .checkedAt(checkIn.getCheckedAt())
                .build();
    }
}
