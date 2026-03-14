package com.project.api.dto.guardian;

import com.project.api.domain.Guardian;

public record GuardianResponse(
        Long id,
        String name,
        String phone,
        String relationship
) {
    public static GuardianResponse from(Guardian guardian) {
        return new GuardianResponse(
                guardian.getId(),
                guardian.getName(),
                guardian.getPhone(),
                guardian.getRelationship()
        );
    }
}
