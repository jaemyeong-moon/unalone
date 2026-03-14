package com.project.api.service;

import com.project.api.domain.Comment;
import com.project.api.domain.CommunityPost;
import com.project.api.domain.User;
import com.project.api.domain.enums.NotificationType;
import com.project.api.dto.comment.CommentRequest;
import com.project.api.dto.comment.CommentResponse;
import com.project.api.exception.BusinessException;
import com.project.api.repository.CommentRepository;
import com.project.api.repository.CommunityPostRepository;
import com.project.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommunityPostRepository postRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * 댓글을 작성합니다. 게시글 작성자가 다른 경우 알림을 전송합니다.
     */
    @Transactional
    public CommentResponse createComment(Long userId, Long postId, CommentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> BusinessException.notFound("게시글을 찾을 수 없습니다"));

        // 대댓글인 경우 부모 댓글 존재 여부 확인
        if (request.parentId() != null) {
            commentRepository.findById(request.parentId())
                    .orElseThrow(() -> BusinessException.notFound("부모 댓글을 찾을 수 없습니다"));
        }

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(request.content())
                .parentId(request.parentId())
                .build();

        commentRepository.save(comment);
        log.info("댓글 작성: userId={}, postId={}, commentId={}", userId, postId, comment.getId());

        // 게시글 작성자에게 알림 (본인 댓글 제외)
        if (!post.getUser().getId().equals(userId)) {
            notificationService.createNotification(
                    post.getUser().getId(),
                    NotificationType.COMMUNITY_REPLY,
                    "새 댓글",
                    user.getName() + "님이 '" + post.getTitle() + "' 게시글에 댓글을 남겼습니다.",
                    postId,
                    "COMMUNITY_POST"
            );
        }

        return CommentResponse.from(comment);
    }

    /**
     * 게시글의 댓글 목록을 조회합니다 (계층 구조).
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId) {
        // 게시글 존재 확인
        if (!postRepository.existsById(postId)) {
            throw BusinessException.notFound("게시글을 찾을 수 없습니다");
        }

        // 최상위 댓글 조회
        List<Comment> topLevelComments = commentRepository.findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(postId);

        return topLevelComments.stream()
                .map(comment -> {
                    List<CommentResponse> replies = commentRepository
                            .findByParentIdOrderByCreatedAtAsc(comment.getId())
                            .stream()
                            .map(CommentResponse::from)
                            .toList();
                    return CommentResponse.from(comment, replies);
                })
                .toList();
    }

    /**
     * 댓글을 수정합니다. 작성자만 수정 가능합니다.
     */
    @Transactional
    public CommentResponse updateComment(Long userId, Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> BusinessException.notFound("댓글을 찾을 수 없습니다"));

        if (!comment.getUser().getId().equals(userId)) {
            throw BusinessException.unauthorized("본인의 댓글만 수정할 수 있습니다");
        }

        comment.updateContent(content);
        commentRepository.save(comment);
        return CommentResponse.from(comment);
    }

    /**
     * 댓글을 삭제합니다. 작성자만 삭제 가능합니다.
     */
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> BusinessException.notFound("댓글을 찾을 수 없습니다"));

        if (!comment.getUser().getId().equals(userId)) {
            throw BusinessException.unauthorized("본인의 댓글만 삭제할 수 있습니다");
        }

        commentRepository.delete(comment);
    }
}
