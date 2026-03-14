package com.project.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.api.domain.CommunityPost;
import com.project.api.domain.PostQualityLog;
import com.project.api.domain.User;
import com.project.api.dto.community.CommunityPostRequest;
import com.project.api.dto.community.CommunityPostResponse;
import com.project.api.dto.quality.QualityDetailResponse;
import com.project.api.exception.BusinessException;
import com.project.api.repository.CommentRepository;
import com.project.api.repository.CommunityPostRepository;
import com.project.api.repository.PostQualityLogRepository;
import com.project.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityPostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostQualityLogRepository qualityLogRepository;
    private final ObjectMapper objectMapper;

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
                    .map(post -> CommunityPostResponse.from(post, commentRepository.countByPostId(post.getId())));
        }
        return postRepository.findAllWithUser(pageable)
                .map(post -> CommunityPostResponse.from(post, commentRepository.countByPostId(post.getId())));
    }

    @Transactional(readOnly = true)
    public CommunityPostResponse getPost(Long postId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> BusinessException.notFound("게시글을 찾을 수 없습니다"));
        return CommunityPostResponse.from(post, commentRepository.countByPostId(postId));
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

    @Transactional(readOnly = true)
    public Page<CommunityPostResponse> getPostsByQuality(Pageable pageable) {
        return postRepository.findAllByQualityScoreDesc(pageable)
                .map(post -> CommunityPostResponse.from(post, commentRepository.countByPostId(post.getId())));
    }

    @Transactional(readOnly = true)
    public QualityDetailResponse getPostQualityDetail(Long postId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> BusinessException.notFound("게시글을 찾을 수 없습니다"));

        List<PostQualityLog> logs = qualityLogRepository.findByPostIdOrderByCreatedAtDesc(post.getId());
        if (logs.isEmpty()) {
            throw BusinessException.notFound("품질 점수가 아직 산출되지 않았습니다");
        }

        PostQualityLog latestLog = logs.get(0);
        Map<String, Integer> breakdown = deserializeBreakdown(latestLog.getScoringDetails());

        return QualityDetailResponse.from(latestLog, breakdown);
    }

    private Map<String, Integer> deserializeBreakdown(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (JsonProcessingException e) {
            log.error("점수 상세 역직렬화 실패: {}", e.getMessage());
            return Collections.emptyMap();
        }
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
