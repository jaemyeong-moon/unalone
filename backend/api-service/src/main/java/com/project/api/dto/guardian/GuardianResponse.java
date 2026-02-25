package com.project.api.dto.guardian;

import com.project.api.domain.Guardian;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardianResponse {
    private Long id;
    private String name;
    private String phone;
    private String relationship;

    public static GuardianResponse from(Guardian guardian) {
        return GuardianResponse.builder()
                .id(guardian.getId())
                .name(guardian.getName())
                .phone(guardian.getPhone())
                .relationship(guardian.getRelationship())
                .build();
    }
}
