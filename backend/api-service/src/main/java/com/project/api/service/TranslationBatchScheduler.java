package com.project.api.service;

import com.project.api.domain.CommunityPost;
import com.project.api.domain.TranslationStatus;
import com.project.api.dto.translation.TranslationResult;
import com.project.api.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 번역 대기 중인 게시글을 주기적으로 번역 처리하는 배치 스케줄러.
 * 30분마다 실행되며, PENDING 및 재시도 가능한 FAILED 상태의 게시글을 처리합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TranslationBatchScheduler {

    private final CommunityPostRepository postRepository;
    private final TranslationService translationService;

    @Value("${translation.enabled:true}")
    private boolean translationEnabled;

    @Value("${translation.target-language:en}")
    private String targetLanguage;

    @Value("${translation.batch-size:50}")
    private int batchSize;

    @Value("${translation.max-retries:3}")
    private int maxRetries;

    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void translatePendingPosts() {
        if (!translationEnabled) {
            log.debug("번역 기능이 비활성화되어 있습니다");
            return;
        }

        log.info("번역 배치 스케줄러 시작");

        // PENDING 상태 게시글 조회
        List<CommunityPost> pendingPosts = postRepository.findByTranslationStatus(
                TranslationStatus.PENDING, PageRequest.of(0, batchSize));

        // 재시도 가능한 FAILED 상태 게시글 조회
        List<CommunityPost> failedPosts = postRepository.findByTranslationStatusAndTranslationRetryCountLessThan(
                TranslationStatus.FAILED, maxRetries, PageRequest.of(0, batchSize / 5));

        int translatedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (CommunityPost post : pendingPosts) {
            try {
                boolean result = translatePost(post);
                if (result) translatedCount++;
                else skippedCount++;
            } catch (Exception e) {
                log.error("게시글 번역 실패 (postId={}): {}", post.getId(), e.getMessage());
                post.markTranslationFailed();
                failedCount++;
            }
        }

        for (CommunityPost post : failedPosts) {
            try {
                boolean result = translatePost(post);
                if (result) translatedCount++;
                else skippedCount++;
            } catch (Exception e) {
                log.error("게시글 번역 재시도 실패 (postId={}): {}", post.getId(), e.getMessage());
                post.markTranslationFailed();
                failedCount++;
            }
        }

        log.info("번역 배치 완료 - 번역: {}, 건너뜀: {}, 실패: {}, 총 처리: {}",
                translatedCount, skippedCount, failedCount,
                translatedCount + skippedCount + failedCount);
    }

    /**
     * 단일 게시글을 번역합니다.
     * @return true: 번역 완료, false: 건너뜀
     */
    private boolean translatePost(CommunityPost post) {
        String detectedLanguage = translationService.detectLanguage(post.getContent());

        // 이미 대상 언어인 경우 건너뜀
        if (detectedLanguage.equals(targetLanguage)) {
            post.markTranslationSkipped(detectedLanguage);
            return false;
        }

        // 제목 번역
        TranslationResult titleResult = translationService.translate(
                post.getTitle(), detectedLanguage, targetLanguage);

        // 본문 번역
        TranslationResult contentResult = translationService.translate(
                post.getContent(), detectedLanguage, targetLanguage);

        if (titleResult.success() && contentResult.success()) {
            post.updateTranslation(
                    titleResult.translatedText(),
                    contentResult.translatedText(),
                    detectedLanguage);
            return true;
        } else {
            post.markTranslationFailed();
            String errorMsg = !titleResult.success() ? titleResult.errorMessage() : contentResult.errorMessage();
            log.warn("번역 실패 (postId={}): {}", post.getId(), errorMsg);
            return false;
        }
    }
}
