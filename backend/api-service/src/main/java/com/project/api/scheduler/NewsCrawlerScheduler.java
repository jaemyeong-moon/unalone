package com.project.api.scheduler;

import com.project.api.service.ArticlePublishService;
import com.project.api.service.ArticleSummaryService;
import com.project.api.service.NewsCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsCrawlerScheduler {

    private final NewsCrawlerService newsCrawlerService;
    private final ArticleSummaryService articleSummaryService;
    private final ArticlePublishService articlePublishService;

    @Value("${crawling.enabled:true}")
    private boolean crawlingEnabled;

    /**
     * 6시간마다 뉴스 크롤링을 실행한다.
     * 1. 활성화된 모든 소스에서 크롤링
     * 2. 크롤링된 기사에 대해 요약 생성 및 품질 점수 계산
     * 3. 품질 기준을 충족하는 기사를 자동 게시
     */
    @Scheduled(cron = "0 0 */6 * * *")
    public void scheduledCrawl() {
        if (!crawlingEnabled) {
            log.info("뉴스 크롤링이 비활성화되어 있습니다");
            return;
        }

        log.info("===== 뉴스 크롤링 스케줄러 시작 =====");

        try {
            // 1단계: 크롤링
            int crawledCount = newsCrawlerService.crawlAllSources();
            log.info("크롤링 완료: {}건 수집", crawledCount);

            // 2단계: 요약 및 점수 계산
            int processedCount = articleSummaryService.processAllCrawledArticles();
            log.info("기사 처리 완료: {}건 처리", processedCount);

            // 3단계: 자동 게시
            int publishedCount = articlePublishService.autoPublishArticles();
            log.info("자동 게시 완료: {}건 게시", publishedCount);

        } catch (Exception e) {
            log.error("뉴스 크롤링 스케줄러 오류: {}", e.getMessage(), e);
        }

        log.info("===== 뉴스 크롤링 스케줄러 종료 =====");
    }
}
