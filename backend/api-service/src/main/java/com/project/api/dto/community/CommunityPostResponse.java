package com.project.api.dto.community;

import com.project.api.domain.CommunityPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String title;
    private String content;
    private String category;
    private LocalDateTime createdAt;

    public static CommunityPostResponse from(CommunityPost post) {
        return CommunityPostResponse.builder()
                .id(post.getId())
                .userId(post.getUser().getId())
                .userName(post.getUser().getName())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory().name())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
