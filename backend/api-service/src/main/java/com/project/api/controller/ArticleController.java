package com.project.api.controller;

import com.project.api.dto.article.ArticleDetailResponse;
import com.project.api.dto.article.CrawledArticleResponse;
import com.project.api.service.ArticleService;
import com.project.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CrawledArticleResponse>>> getArticles(
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(articleService.getPublishedArticles(category, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleDetailResponse>> getArticle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(articleService.getArticleDetail(id)));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<CrawledArticleResponse>>> getPopularArticles() {
        return ResponseEntity.ok(ApiResponse.ok(articleService.getPopularArticles()));
    }
}
