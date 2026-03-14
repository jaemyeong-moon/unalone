package com.project.admin.dto;

import com.project.admin.domain.CommunityPost;

import java.time.LocalDateTime;

public record QualityPostResponse(
        Long id,
        Long userId,
        String userName,
        String title,
        String content,
        String category,
        Integer qualityScore,
        String qualityGrade,
        LocalDateTime scoredAt,
        LocalDateTime createdAt
) {
    public static QualityPostResponse from(CommunityPost post) {
        return new QualityPostResponse(
                post.getId(),
                post.getUser().getId(),
                post.getUser().getName(),
                post.getTitle(),
                post.getContent(),
                post.getCategory().name(),
                post.getQualityScore(),
                post.getQualityGrade() != null ? post.getQualityGrade().name() : null,
                post.getScoredAt(),
                post.getCreatedAt()
        );
    }
}
