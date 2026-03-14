package com.project.api.dto.community;

import com.project.api.domain.CommunityPost;

import java.time.LocalDateTime;

public record CommunityPostResponse(
        Long id,
        Long userId,
        String userName,
        String title,
        String content,
        String category,
        long commentCount,
        LocalDateTime createdAt
) {
    public static CommunityPostResponse from(CommunityPost post) {
        return new CommunityPostResponse(
                post.getId(),
                post.getUser().getId(),
                post.getUser().getName(),
                post.getTitle(),
                post.getContent(),
                post.getCategory().name(),
                0,
                post.getCreatedAt()
        );
    }

    public static CommunityPostResponse from(CommunityPost post, long commentCount) {
        return new CommunityPostResponse(
                post.getId(),
                post.getUser().getId(),
                post.getUser().getName(),
                post.getTitle(),
                post.getContent(),
                post.getCategory().name(),
                commentCount,
                post.getCreatedAt()
        );
    }
}
