package com.project.api.service;

import com.project.api.domain.CommunityPost;
import com.project.api.domain.User;
import com.project.api.dto.community.CommunityPostRequest;
import com.project.api.dto.community.CommunityPostResponse;
import com.project.api.exception.BusinessException;
import com.project.api.repository.CommunityPostRepository;
import com.project.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityPostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommunityPostResponse createPost(Long userId, CommunityPostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        CommunityPost.PostCategory category = CommunityPost.PostCategory.DAILY;
        if (request.getCategory() != null) {
            try {
                category = CommunityPost.PostCategory.valueOf(request.getCategory());
            } catch (IllegalArgumentException ignored) {
            }
        }

        CommunityPost post = CommunityPost.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .category(category)
                .build();

        postRepository.save(post);
        return CommunityPostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public Page<CommunityPostResponse> getPosts(String category, Pageable pageable) {
        if (category != null && !category.isEmpty()) {
            try {
                CommunityPost.PostCategory cat = CommunityPost.PostCategory.valueOf(category);
                return postRepository.findByCategoryWithUser(cat, pageable)
                        .map(CommunityPostResponse::from);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return postRepository.findAllWithUser(pageable)
                .map(CommunityPostResponse::from);
    }

    @Transactional(readOnly = true)
    public CommunityPostResponse getPost(Long postId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> BusinessException.notFound("게시글을 찾을 수 없습니다"));
        return CommunityPostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> BusinessException.notFound("게시글을 찾을 수 없습니다"));

        if (!post.getUser().getId().equals(userId)) {
            throw BusinessException.unauthorized("본인의 게시글만 삭제할 수 있습니다");
        }

        postRepository.delete(post);
    }
}
