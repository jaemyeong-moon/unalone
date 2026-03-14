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
        LocalDateTime createdAt,
        // Translation fields
        String translatedTitle,
        String translatedContent,
        String originalLanguage,
        String translationStatus,
        // Quality scoring fields
        Integer qualityScore,
        String qualityGrade
) {
    public static CommunityPostResponse from(CommunityPost post) {
        return from(post, 0);
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
                post.getCreatedAt(),
                post.getTranslatedTitle(),
                post.getTranslatedContent(),
                post.getOriginalLanguage(),
                post.getTranslationStatus() != null ? post.getTranslationStatus().name() : null,
                post.getQualityScore(),
                post.getQualityGrade() != null ? post.getQualityGrade().name() : null
        );
    }
}
