package com.project.api.dto.profile;

import com.project.api.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Long userId;
    private String userName;
    private String email;
    private String phone;
    private Integer checkIntervalHours;
    private String activeHoursStart;
    private String activeHoursEnd;
    private String address;
    private String emergencyNote;

    public static ProfileResponse from(Profile profile) {
        return ProfileResponse.builder()
                .userId(profile.getUser().getId())
                .userName(profile.getUser().getName())
                .email(profile.getUser().getEmail())
                .phone(profile.getUser().getPhone())
                .checkIntervalHours(profile.getCheckIntervalHours())
                .activeHoursStart(profile.getActiveHoursStart())
                .activeHoursEnd(profile.getActiveHoursEnd())
                .address(profile.getAddress())
                .emergencyNote(profile.getEmergencyNote())
                .build();
    }
}
