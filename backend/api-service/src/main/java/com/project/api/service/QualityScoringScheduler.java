package com.project.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.api.domain.CommunityPost;
import com.project.api.domain.PostQualityLog;
import com.project.api.dto.quality.QualityScoreResult;
import com.project.api.repository.CommunityPostRepository;
import com.project.api.repository.PostQualityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 품질 미평가 게시글을 주기적으로 점수 산출하는 배치 스케줄러.
 * 15분마다 실행되며, qualityScore가 null인 게시글을 처리합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class QualityScoringScheduler {

    private final CommunityPostRepository postRepository;
    private final ContentQualityService qualityService;
    private final PostQualityLogRepository qualityLogRepository;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void scorePendingPosts() {
        log.info("품질 점수 산출 배치 스케줄러 시작");

        List<CommunityPost> unscoredPosts = postRepository.findByQualityScoreIsNull(
                PageRequest.of(0, 100));

        int scoredCount = 0;
        int spamCount = 0;

        for (CommunityPost post : unscoredPosts) {
            try {
                QualityScoreResult result = qualityService.scorePost(post);

                // 게시글에 점수 반영
                post.updateQualityScore(result.score(), result.grade());

                // 점수 산출 이력 저장
                String detailsJson = serializeBreakdown(result);
                PostQualityLog qualityLog = PostQualityLog.builder()
                        .postId(post.getId())
                        .score(result.score())
                        .grade(result.grade())
                        .scoringDetails(detailsJson)
                        .build();
                qualityLogRepository.save(qualityLog);

                scoredCount++;

                // SPAM 등급 게시글 로그 경고
                if (result.score() < 30) {
                    spamCount++;
                    log.warn("SPAM 의심 게시글 감지 - postId: {}, score: {}, grade: {}",
                            post.getId(), result.score(), result.grade());
                }
            } catch (Exception e) {
                log.error("게시글 품질 점수 산출 실패 (postId={}): {}", post.getId(), e.getMessage());
            }
        }

        log.info("품질 점수 산출 배치 완료 - 처리: {}, SPAM 의심: {}", scoredCount, spamCount);
    }

    private String serializeBreakdown(QualityScoreResult result) {
        try {
            return objectMapper.writeValueAsString(result.breakdown());
        } catch (JsonProcessingException e) {
            log.error("점수 상세 직렬화 실패: {}", e.getMessage());
            return "{}";
        }
    }
}
