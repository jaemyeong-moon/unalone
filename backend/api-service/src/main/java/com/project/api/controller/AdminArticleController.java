package com.project.api.controller;

import com.project.api.domain.ArticleCategory;
import com.project.api.domain.NewsSource;
import com.project.api.dto.article.CrawlStatsResponse;
import com.project.api.dto.article.CrawledArticleResponse;
import com.project.api.dto.article.NewsSourceRequest;
import com.project.api.dto.article.NewsSourceResponse;
import com.project.api.exception.BusinessException;
import com.project.api.repository.NewsSourceRepository;
import com.project.api.service.ArticlePublishService;
import com.project.api.service.ArticleService;
import com.project.api.service.ArticleSummaryService;
import com.project.api.service.NewsCrawlerService;
import com.project.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final NewsSourceRepository newsSourceRepository;
    private final NewsCrawlerService newsCrawlerService;
    private final ArticleSummaryService articleSummaryService;
    private final ArticlePublishService articlePublishService;
    private final ArticleService articleService;

    // ===== 뉴스 소스 관리 =====

    @GetMapping("/sources")
    public ResponseEntity<ApiResponse<List<NewsSourceResponse>>> getSources() {
        List<NewsSourceResponse> sources = newsSourceRepository.findAll().stream()
                .map(NewsSourceResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(sources));
    }

    @PostMapping("/sources")
    public ResponseEntity<ApiResponse<NewsSourceResponse>> createSource(
            @Valid @RequestBody NewsSourceRequest request) {
        ArticleCategory category = parseCategory(request.category());

        NewsSource source = NewsSource.builder()
                .name(request.name())
                .baseUrl(request.baseUrl())
                .crawlPattern(request.crawlPattern())
                .articlePattern(request.articlePattern())
                .category(category)
                .enabled(request.enabled() != null ? request.enabled() : true)
                .build();

        newsSourceRepository.save(source);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(NewsSourceResponse.from(source), "뉴스 소스 등록 완료"));
    }

    @PutMapping("/sources/{id}")
    public ResponseEntity<ApiResponse<NewsSourceResponse>> updateSource(
            @PathVariable Long id,
            @RequestBody NewsSourceRequest request) {
        NewsSource source = newsSourceRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("뉴스 소스를 찾을 수 없습니다"));

        ArticleCategory category = request.category() != null ? parseCategory(request.category()) : null;
        source.update(request.name(), request.baseUrl(), request.crawlPattern(),
                request.articlePattern(), category, request.enabled());

        newsSourceRepository.save(source);
        return ResponseEntity.ok(ApiResponse.ok(NewsSourceResponse.from(source), "뉴스 소스 수정 완료"));
    }

    @DeleteMapping("/sources/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSource(@PathVariable Long id) {
        if (!newsSourceRepository.existsById(id)) {
            throw BusinessException.notFound("뉴스 소스를 찾을 수 없습니다");
        }
        newsSourceRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "뉴스 소스 삭제 완료"));
    }

    // ===== 크롤링 관리 =====

    @PostMapping("/crawl")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> triggerCrawl() {
        int crawledCount = newsCrawlerService.crawlAllSources();
        int processedCount = articleSummaryService.processAllCrawledArticles();
        int publishedCount = articlePublishService.autoPublishArticles();

        Map<String, Integer> result = Map.of(
                "crawled", crawledCount,
                "processed", processedCount,
                "published", publishedCount
        );

        return ResponseEntity.ok(ApiResponse.ok(result, "크롤링 실행 완료"));
    }

    // ===== 기사 관리 =====

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CrawledArticleResponse>>> getAllArticles(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(articleService.getAllArticles(status, pageable)));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<Void>> publishArticle(@PathVariable Long id) {
        articlePublishService.publishArticle(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "기사 게시 완료"));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectArticle(@PathVariable Long id) {
        articleService.rejectArticle(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "기사 거절 완료"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<CrawlStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(articleService.getStats()));
    }

    private ArticleCategory parseCategory(String category) {
        try {
            return ArticleCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            throw BusinessException.badRequest("잘못된 카테고리입니다: " + category);
        }
    }
}
