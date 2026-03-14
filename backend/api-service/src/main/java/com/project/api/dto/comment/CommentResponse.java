package com.project.api.dto.comment;

import com.project.api.domain.Comment;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long id,
        Long postId,
        Long userId,
        String userName,
        String content,
        Long parentId,
        List<CommentResponse> replies,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getUser().getId(),
                comment.getUser().getName(),
                comment.getContent(),
                comment.getParentId(),
                List.of(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    public static CommentResponse from(Comment comment, List<CommentResponse> replies) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getUser().getId(),
                comment.getUser().getName(),
                comment.getContent(),
                comment.getParentId(),
                replies,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
