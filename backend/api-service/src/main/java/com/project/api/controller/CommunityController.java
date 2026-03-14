package com.project.api.controller;

import com.project.api.dto.community.CommunityPostRequest;
import com.project.api.dto.community.CommunityPostResponse;
import com.project.api.dto.quality.QualityDetailResponse;
import com.project.api.service.CommunityService;
import com.project.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community/posts")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommunityPostResponse>> createPost(
            Authentication authentication,
            @Valid @RequestBody CommunityPostRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(communityService.createPost(userId, request), "게시글 작성 완료"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CommunityPostResponse>>> getPosts(
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(communityService.getPosts(category, pageable)));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<CommunityPostResponse>> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.ok(communityService.getPost(postId)));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            Authentication authentication,
            @PathVariable Long postId) {
        Long userId = (Long) authentication.getPrincipal();
        communityService.deletePost(userId, postId);
        return ResponseEntity.ok(ApiResponse.ok(null, "게시글 삭제 완료"));
    }

    @GetMapping("/quality")
    public ResponseEntity<ApiResponse<Page<CommunityPostResponse>>> getPostsByQuality(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(communityService.getPostsByQuality(pageable)));
    }

    @GetMapping("/{postId}/quality")
    public ResponseEntity<ApiResponse<QualityDetailResponse>> getPostQualityDetail(
            @PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.ok(communityService.getPostQualityDetail(postId)));
    }
}
