package com.project.api.repository;

import com.project.api.domain.ArticleCategory;
import com.project.api.domain.CrawledArticle;
import com.project.api.domain.CrawledArticle.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CrawledArticleRepository extends JpaRepository<CrawledArticle, Long> {

    List<CrawledArticle> findByStatus(ArticleStatus status);

    Optional<CrawledArticle> findByOriginalUrl(String originalUrl);

    Page<CrawledArticle> findByStatusAndCategoryOrderByPublishedAtDesc(
            ArticleStatus status, ArticleCategory category, Pageable pageable);

    Page<CrawledArticle> findByStatusOrderByCrawledAtDesc(ArticleStatus status, Pageable pageable);

    @Query("SELECT a FROM CrawledArticle a WHERE a.status = :status ORDER BY a.crawledAt DESC")
    Page<CrawledArticle> findAllByStatus(@Param("status") ArticleStatus status, Pageable pageable);

    @Query("SELECT a FROM CrawledArticle a ORDER BY a.viewCount DESC")
    List<CrawledArticle> findTopByOrderByViewCountDesc(Pageable pageable);

    long countByStatusAndCrawledAtAfter(ArticleStatus status, LocalDateTime after);

    long countByStatus(ArticleStatus status);

    List<CrawledArticle> findByStatusAndQualityScoreGreaterThanEqual(ArticleStatus status, int qualityScore);
}
