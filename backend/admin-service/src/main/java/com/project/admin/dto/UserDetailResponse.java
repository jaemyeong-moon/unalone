package com.project.admin.dto;

import com.project.admin.domain.User;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastCheckInAt;

    public static UserDetailResponse from(User user, LocalDateTime lastCheckInAt) {
        return UserDetailResponse.builder()
                .id(user.getId()).email(user.getEmail()).name(user.getName())
                .phone(user.getPhone()).role(user.getRole().name()).status(user.getStatus().name())
                .createdAt(user.getCreatedAt()).lastCheckInAt(lastCheckInAt).build();
    }
}
