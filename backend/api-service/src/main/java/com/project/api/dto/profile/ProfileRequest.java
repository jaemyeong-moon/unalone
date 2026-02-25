package com.project.api.dto.profile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequest {

    @Min(value = 1, message = "체크 주기는 최소 1시간입니다")
    @Max(value = 72, message = "체크 주기는 최대 72시간입니다")
    private Integer checkIntervalHours;

    private String activeHoursStart;
    private String activeHoursEnd;
    private String address;
    private String emergencyNote;
}
