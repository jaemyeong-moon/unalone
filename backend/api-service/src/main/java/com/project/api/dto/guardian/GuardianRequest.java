package com.project.api.dto.guardian;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GuardianRequest {

    @NotBlank(message = "보호자 이름은 필수입니다")
    private String name;

    @NotBlank(message = "보호자 연락처는 필수입니다")
    private String phone;

    private String relationship;
}
