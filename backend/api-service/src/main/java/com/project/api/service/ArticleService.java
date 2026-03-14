package com.project.api.service;

import com.project.api.domain.ArticleCategory;
import com.project.api.domain.CrawledArticle;
import com.project.api.domain.CrawledArticle.ArticleStatus;
import com.project.api.dto.article.ArticleDetailResponse;
import com.project.api.dto.article.CrawlStatsResponse;
import com.project.api.dto.article.CrawledArticleResponse;
import com.project.api.exception.BusinessException;
import com.project.api.repository.CrawledArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final CrawledArticleRepository crawledArticleRepository;

    /**
     * 게시된 기사 목록을 조회한다 (페이지네이션, 카테고리 필터).
     */
    @Transactional(readOnly = true)
    public Page<CrawledArticleResponse> getPublishedArticles(String category, Pageable pageable) {
        if (category != null && !category.isBlank()) {
            try {
                ArticleCategory parsed = ArticleCategory.valueOf(category);
                return crawledArticleRepository.findByStatusAndCategoryOrderByPublishedAtDesc(
                        ArticleStatus.PUBLISHED, parsed, pageable
                ).map(CrawledArticleResponse::from);
            } catch (IllegalArgumentException e) {
                // 잘못된 카테고리는 무시하고 전체 조회
            }
        }
        return crawledArticleRepository.findByStatusOrderByCrawledAtDesc(
                ArticleStatus.PUBLISHED, pageable
        ).map(CrawledArticleResponse::from);
    }

    /**
     * 기사 상세 조회 (조회수 증가).
     */
    @Transactional
    public ArticleDetailResponse getArticleDetail(Long id) {
        CrawledArticle article = crawledArticleRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("기사를 찾을 수 없습니다"));

        article.incrementViewCount();
        crawledArticleRepository.save(article);

        return ArticleDetailResponse.from(article);
    }

    /**
     * 인기 기사 Top 10 조회 (조회수 기준).
     */
    @Transactional(readOnly = true)
    public List<CrawledArticleResponse> getPopularArticles() {
        return crawledArticleRepository.findTopByOrderByViewCountDesc(PageRequest.of(0, 10))
                .stream()
                .map(CrawledArticleResponse::from)
                .toList();
    }

    /**
     * 관리자용: 모든 기사를 상태 관계없이 조회한다.
     */
    @Transactional(readOnly = true)
    public Page<CrawledArticleResponse> getAllArticles(String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            try {
                ArticleStatus parsed = ArticleStatus.valueOf(status);
                return crawledArticleRepository.findAllByStatus(parsed, pageable)
                        .map(CrawledArticleResponse::from);
            } catch (IllegalArgumentException e) {
                // 잘못된 상태값은 무시
            }
        }
        return crawledArticleRepository.findAll(pageable)
                .map(CrawledArticleResponse::from);
    }

    /**
     * 기사를 거절 처리한다.
     */
    @Transactional
    public void rejectArticle(Long id) {
        CrawledArticle article = crawledArticleRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("기사를 찾을 수 없습니다"));
        article.reject();
        crawledArticleRepository.save(article);
    }

    /**
     * 크롤링 통계를 조회한다.
     */
    @Transactional(readOnly = true)
    public CrawlStatsResponse getStats() {
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);

        return new CrawlStatsResponse(
                crawledArticleRepository.count(),
                crawledArticleRepository.countByStatus(ArticleStatus.CRAWLED),
                crawledArticleRepository.countByStatus(ArticleStatus.SUMMARIZED),
                crawledArticleRepository.countByStatus(ArticleStatus.PUBLISHED),
                crawledArticleRepository.countByStatus(ArticleStatus.REJECTED),
                crawledArticleRepository.countByStatus(ArticleStatus.FAILED),
                crawledArticleRepository.countByStatusAndCrawledAtAfter(ArticleStatus.CRAWLED, last24h),
                crawledArticleRepository.countByStatusAndCrawledAtAfter(ArticleStatus.PUBLISHED, last24h)
        );
    }
}
