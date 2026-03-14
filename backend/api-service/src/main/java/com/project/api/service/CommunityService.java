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

        CommunityPost post = CommunityPost.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .category(parseCategory(request.category()))
                .build();

        postRepository.save(post);
        return CommunityPostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public Page<CommunityPostResponse> getPosts(String category, Pageable pageable) {
        if (category != null && !category.isBlank()) {
            CommunityPost.PostCategory parsed = parseCategory(category);
            return postRepository.findByCategoryWithUser(parsed, pageable)
                    .map(CommunityPostResponse::from);
        }
        return postRepository.findAllWithUser(pageable)
                .map(CommunityPostResponse::from);
    }

    @Transactional(readOnly = true)
    public CommunityPostResponse getPost(Long postId) {
        return postRepository.findById(postId)
                .map(CommunityPostResponse::from)
                .orElseThrow(() -> BusinessException.notFound("게시글을 찾을 수 없습니다"));
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

    // 카테고리 문자열을 enum으로 변환; 잘못된 값은 기본값(DAILY)으로 대체
    private CommunityPost.PostCategory parseCategory(String category) {
        if (category == null) {
            return CommunityPost.PostCategory.DAILY;
        }
        try {
            return CommunityPost.PostCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            return CommunityPost.PostCategory.DAILY;
        }
    }
}
