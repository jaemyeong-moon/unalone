package com.project.api.service;

import com.project.api.domain.CommunityPost;
import com.project.api.domain.CrawledArticle;
import com.project.api.domain.CrawledArticle.ArticleStatus;
import com.project.api.domain.User;
import com.project.api.repository.CommunityPostRepository;
import com.project.api.repository.CrawledArticleRepository;
import com.project.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticlePublishService {

    private final CrawledArticleRepository crawledArticleRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;

    @Value("${crawling.auto-publish-threshold:70}")
    private int autoPublishThreshold;

    private static final String NEWS_BOT_EMAIL = "newsbot@unalone.kr";

    /**
     * 품질 점수가 기준 이상인 SUMMARIZED 기사를 자동으로 커뮤니티에 게시한다.
     */
    @Transactional
    public int autoPublishArticles() {
        List<CrawledArticle> readyArticles = crawledArticleRepository
                .findByStatusAndQualityScoreGreaterThanEqual(ArticleStatus.SUMMARIZED, autoPublishThreshold);

        log.info("자동 게시 대상 기사 수: {}", readyArticles.size());

        // 뉴스봇 사용자 조회 또는 생성
        User newsBot = getOrCreateNewsBot();

        int publishedCount = 0;
        for (CrawledArticle article : readyArticles) {
            try {
                // 커뮤니티 게시글 생성
                String categoryLabel = mapCategoryLabel(article.getCategory().name());
                String title = "[" + categoryLabel + "] " + article.getOriginalTitle();
                if (title.length() > 100) {
                    title = title.substring(0, 97) + "...";
                }

                String content = buildPostContent(article);

                CommunityPost post = CommunityPost.builder()
                        .user(newsBot)
                        .title(title)
                        .content(content)
                        .category(CommunityPost.PostCategory.NOTICE)
                        .build();

                communityPostRepository.save(post);

                // 기사 상태를 PUBLISHED로 변경
                article.publish();
                crawledArticleRepository.save(article);

                publishedCount++;
                log.info("기사 자동 게시 완료: {}", article.getOriginalTitle());

            } catch (Exception e) {
                log.error("기사 게시 실패 - 제목: {}, 오류: {}", article.getOriginalTitle(), e.getMessage());
            }
        }

        log.info("자동 게시 완료. 총 {}건 게시", publishedCount);
        return publishedCount;
    }

    /**
     * 특정 기사를 수동으로 게시한다.
     */
    @Transactional
    public void publishArticle(Long articleId) {
        CrawledArticle article = crawledArticleRepository.findById(articleId)
                .orElseThrow(() -> new com.project.api.exception.BusinessException(
                        "기사를 찾을 수 없습니다", org.springframework.http.HttpStatus.NOT_FOUND));

        User newsBot = getOrCreateNewsBot();

        String categoryLabel = mapCategoryLabel(article.getCategory().name());
        String title = "[" + categoryLabel + "] " + article.getOriginalTitle();
        if (title.length() > 100) {
            title = title.substring(0, 97) + "...";
        }

        String content = buildPostContent(article);

        CommunityPost post = CommunityPost.builder()
                .user(newsBot)
                .title(title)
                .content(content)
                .category(CommunityPost.PostCategory.NOTICE)
                .build();

        communityPostRepository.save(post);
        article.publish();
        crawledArticleRepository.save(article);

        log.info("기사 수동 게시 완료: {}", article.getOriginalTitle());
    }

    /**
     * 뉴스봇 사용자를 조회하거나 없으면 생성한다.
     */
    private User getOrCreateNewsBot() {
        return userRepository.findByEmail(NEWS_BOT_EMAIL)
                .orElseGet(() -> {
                    User bot = User.builder()
                            .email(NEWS_BOT_EMAIL)
                            .name("뉴스봇")
                            .role(User.Role.ROLE_ADMIN)
                            .build();
                    return userRepository.save(bot);
                });
    }

    /**
     * 커뮤니티 게시글 본문을 구성한다.
     */
    private String buildPostContent(CrawledArticle article) {
        StringBuilder sb = new StringBuilder();

        if (article.getSummary() != null && !article.getSummary().isBlank()) {
            sb.append(article.getSummary());
        } else {
            // 요약이 없으면 원문 앞부분 사용
            String content = article.getOriginalContent();
            if (content.length() > 500) {
                content = content.substring(0, 497) + "...";
            }
            sb.append(content);
        }

        sb.append("\n\n---\n\n원문: ").append(article.getOriginalUrl());

        if (article.getAuthor() != null && !article.getAuthor().isBlank()) {
            sb.append("\n작성자: ").append(article.getAuthor());
        }

        return sb.toString();
    }

    /**
     * 영문 카테고리를 한글 라벨로 변환한다.
     */
    private String mapCategoryLabel(String category) {
        return switch (category) {
            case "HEALTH" -> "건강";
            case "WELFARE" -> "복지";
            case "ELDERLY_CARE" -> "돌봄";
            case "SAFETY" -> "안전";
            case "POLICY" -> "정책";
            case "LIFESTYLE" -> "생활";
            default -> "뉴스";
        };
    }
}
