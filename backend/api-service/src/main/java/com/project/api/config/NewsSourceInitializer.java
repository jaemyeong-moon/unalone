package com.project.api.config;

import com.project.api.domain.ArticleCategory;
import com.project.api.domain.NewsSource;
import com.project.api.repository.NewsSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsSourceInitializer implements CommandLineRunner {

    private final NewsSourceRepository newsSourceRepository;

    @Override
    public void run(String... args) {
        if (newsSourceRepository.count() > 0) {
            log.info("뉴스 소스가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("기본 뉴스 소스를 초기화합니다...");

        // 1. 복지타임즈
        newsSourceRepository.save(NewsSource.builder()
                .name("복지타임즈")
                .baseUrl("http://www.bokjitimes.com")
                .crawlPattern("div.article-list a, ul.news-list a")
                .articlePattern("div.article-body, div.view-content")
                .category(ArticleCategory.WELFARE)
                .enabled(true)
                .build());

        // 2. 메디게이트뉴스
        newsSourceRepository.save(NewsSource.builder()
                .name("메디게이트뉴스")
                .baseUrl("https://www.medigatenews.com")
                .crawlPattern("div.list-block a, ul.article-list a")
                .articlePattern("div.article-content, div.news-content")
                .category(ArticleCategory.HEALTH)
                .enabled(true)
                .build());

        // 3. 에이징뉴스
        newsSourceRepository.save(NewsSource.builder()
                .name("에이징뉴스")
                .baseUrl("http://www.agingnews.co.kr")
                .crawlPattern("div.list-area a, ul.news-list a")
                .articlePattern("div.view-area, div.article-view")
                .category(ArticleCategory.ELDERLY_CARE)
                .enabled(true)
                .build());

        // 4. 안전신문
        newsSourceRepository.save(NewsSource.builder()
                .name("안전신문")
                .baseUrl("http://www.safetynews.co.kr")
                .crawlPattern("div.article-list a, ul.list-block a")
                .articlePattern("div.article-body, div.view-content")
                .category(ArticleCategory.SAFETY)
                .enabled(true)
                .build());

        // 5. 복지뉴스
        newsSourceRepository.save(NewsSource.builder()
                .name("복지뉴스")
                .baseUrl("http://www.bokjinews.com")
                .crawlPattern("div.list-block a, ul.article-list a")
                .articlePattern("div.article-content, div.view-body")
                .category(ArticleCategory.POLICY)
                .enabled(true)
                .build());

        log.info("기본 뉴스 소스 5건 초기화 완료");
    }
}
