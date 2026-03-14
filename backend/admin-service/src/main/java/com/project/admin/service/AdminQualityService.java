package com.project.admin.service;

import com.project.admin.domain.CommunityPost;
import com.project.admin.domain.QualityGrade;
import com.project.admin.dto.QualityOverrideRequest;
import com.project.admin.dto.QualityPostResponse;
import com.project.admin.dto.QualityStatsResponse;
import com.project.admin.exception.InvalidRequestException;
import com.project.admin.exception.ResourceNotFoundException;
import com.project.admin.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminQualityService {

    private final CommunityPostRepository postRepository;

    @Transactional(readOnly = true)
    public QualityStatsResponse getQualityStats() {
        Double avgScore = postRepository.averageQualityScore();
        long totalScored = postRepository.countScoredPosts();
        List<Object[]> gradeData = postRepository.countByQualityGradeGrouped();

        Map<String, Long> distribution = new LinkedHashMap<>();
        for (QualityGrade grade : QualityGrade.values()) {
            distribution.put(grade.name(), 0L);
        }
        for (Object[] row : gradeData) {
            QualityGrade grade = (QualityGrade) row[0];
            Long count = (Long) row[1];
            distribution.put(grade.name(), count);
        }

        return new QualityStatsResponse(
                avgScore != null ? avgScore : 0.0,
                totalScored,
                distribution
        );
    }

    @Transactional(readOnly = true)
    public Page<QualityPostResponse> getFlaggedPosts(Pageable pageable) {
        return postRepository.findByQualityScoreLessThanWithUser(30, pageable)
                .map(QualityPostResponse::from);
    }

    @Transactional
    public void overrideQualityGrade(Long postId, QualityOverrideRequest request) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다: " + postId));

        QualityGrade grade;
        try {
            grade = QualityGrade.valueOf(request.grade());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("유효하지 않은 품질 등급: " + request.grade());
        }

        post.updateQualityGrade(grade);
        log.info("관리자 품질 등급 수동 변경 - postId: {}, grade: {}", postId, grade);
    }
}
