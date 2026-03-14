package com.project.api.dto.profile;

import com.project.api.domain.Profile;

public record ProfileResponse(
        Long userId,
        String userName,
        String email,
        String phone,
        Integer checkIntervalHours,
        String activeHoursStart,
        String activeHoursEnd,
        String address,
        String emergencyNote
) {
    public static ProfileResponse from(Profile profile) {
        return new ProfileResponse(
                profile.getUser().getId(),
                profile.getUser().getName(),
                profile.getUser().getEmail(),
                profile.getUser().getPhone(),
                profile.getCheckIntervalHours(),
                profile.getActiveHoursStart(),
                profile.getActiveHoursEnd(),
                profile.getAddress(),
                profile.getEmergencyNote()
        );
    }
}
