package com.project.api.controller;

import com.project.api.dto.comment.CommentRequest;
import com.project.api.dto.comment.CommentResponse;
import com.project.api.service.CommentService;
import com.project.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            Authentication authentication,
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(commentService.createComment(userId, postId, request), "댓글 작성 완료"));
    }

    /**
     * 게시글의 댓글 목록 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.ok(commentService.getComments(postId)));
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            Authentication authentication,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(commentService.updateComment(userId, commentId, request.content()), "댓글 수정 완료"));
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            Authentication authentication,
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        Long userId = (Long) authentication.getPrincipal();
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.ok(ApiResponse.ok(null, "댓글 삭제 완료"));
    }
}
