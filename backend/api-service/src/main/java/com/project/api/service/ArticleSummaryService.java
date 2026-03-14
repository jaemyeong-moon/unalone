package com.project.api.service;

import com.project.api.domain.CrawledArticle;
import com.project.api.repository.CrawledArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleSummaryService {

    private final CrawledArticleRepository crawledArticleRepository;

    // 관련성 평가에 사용할 키워드 목록
    private static final List<String> RELEVANCE_KEYWORDS = List.of(
            "고독사", "1인가구", "1인 가구", "돌봄", "복지", "건강", "노인",
            "안전", "독거", "사회안전망", "긴급복지", "방문돌봄", "안부",
            "고립", "사회적 고립", "무연사", "취약계층", "생활지원",
            "응급", "위기관리", "사회복지사", "보건", "의료"
    );

    /**
     * 기사 원문에서 요약을 생성한다.
     * 규칙 기반 접근: 의미 있는 문단을 추출하여 요약으로 활용한다.
     */
    public String generateSummary(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        // 문단 단위로 분리
        String[] paragraphs = content.split("\\n+|(?<=\\.)\\s+(?=[가-힣A-Z])");

        StringBuilder summary = new StringBuilder();
        int meaningfulCount = 0;

        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            // 30자 미만의 짧은 문단은 건너뜀 (광고, 부가정보 등)
            if (trimmed.length() < 30) continue;

            if (meaningfulCount > 0) {
                summary.append("\n");
            }
            summary.append(trimmed);
            meaningfulCount++;

            // 최대 3개의 의미 있는 문단까지
            if (meaningfulCount >= 3) break;
        }

        // 최대 500자로 제한
        String result = summary.toString();
        if (result.length() > 500) {
            result = result.substring(0, 497) + "...";
        }

        return result;
    }

    /**
     * 기사 품질 점수를 계산한다 (0-100).
     */
    public int calculateQualityScore(CrawledArticle article) {
        int score = 0;

        String content = article.getOriginalContent();
        if (content == null) content = "";

        // 본문 길이 (200자 이상이면 내용이 충분): +20
        if (content.length() > 200) {
            score += 20;
        }

        // 작성자 정보 존재: +10
        if (article.getAuthor() != null && !article.getAuthor().isBlank()) {
            score += 10;
        }

        // 발행 7일 이내 (신선도): +15
        if (article.getPublishedAt() != null &&
                article.getPublishedAt().isAfter(LocalDateTime.now().minusDays(7))) {
            score += 15;
        }

        // 카테고리 관련성 (키워드 포함 여부): +25
        String textToCheck = (article.getOriginalTitle() + " " + content).toLowerCase();
        long keywordCount = RELEVANCE_KEYWORDS.stream()
                .filter(textToCheck::contains)
                .count();
        if (keywordCount >= 3) {
            score += 25;
        } else if (keywordCount >= 1) {
            score += 15;
        }

        // 제목 중복 여부 (고유 제목): +10
        // 단순히 제목이 비어있지 않으면 가산
        if (article.getOriginalTitle() != null && !article.getOriginalTitle().isBlank()) {
            score += 10;
        }

        // 본문 구조 (여러 문단 존재): +10
        String[] paragraphs = content.split("\\n+|(?<=\\.)\\s+");
        long meaningfulParagraphs = Arrays.stream(paragraphs)
                .filter(p -> p.trim().length() > 30)
                .count();
        if (meaningfulParagraphs >= 3) {
            score += 10;
        }

        // 가독성 (평균 문장 길이가 100자 미만): +10
        String[] sentences = content.split("[.!?。]+");
        if (sentences.length > 0) {
            double avgLength = (double) content.length() / sentences.length;
            if (avgLength < 100) {
                score += 10;
            }
        }

        return Math.min(score, 100);
    }

    /**
     * 크롤링된 기사를 처리한다: 요약 생성 + 품질 점수 계산 + 상태 업데이트.
     */
    @Transactional
    public void processArticle(CrawledArticle article) {
        try {
            String summary = generateSummary(article.getOriginalContent());
            int score = calculateQualityScore(article);
            article.updateSummary(summary, score);
            crawledArticleRepository.save(article);

            log.info("기사 처리 완료 - 제목: {}, 점수: {}, 상태: {}",
                    article.getOriginalTitle(), score, article.getStatus());
        } catch (Exception e) {
            article.fail();
            crawledArticleRepository.save(article);
            log.error("기사 처리 실패 - 제목: {}, 오류: {}", article.getOriginalTitle(), e.getMessage());
        }
    }

    /**
     * CRAWLED 상태의 모든 기사를 처리한다.
     */
    @Transactional
    public int processAllCrawledArticles() {
        List<CrawledArticle> crawledArticles = crawledArticleRepository.findByStatus(
                CrawledArticle.ArticleStatus.CRAWLED);

        log.info("처리 대상 기사 수: {}", crawledArticles.size());

        for (CrawledArticle article : crawledArticles) {
            processArticle(article);
        }

        return crawledArticles.size();
    }
}
