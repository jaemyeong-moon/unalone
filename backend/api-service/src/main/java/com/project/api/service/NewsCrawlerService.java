package com.project.api.service;

import com.project.api.domain.CrawledArticle;
import com.project.api.domain.NewsSource;
import com.project.api.repository.CrawledArticleRepository;
import com.project.api.repository.NewsSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsCrawlerService {

    private final NewsSourceRepository newsSourceRepository;
    private final CrawledArticleRepository crawledArticleRepository;

    @Value("${crawling.user-agent:UnaloneBot/1.0}")
    private String userAgent;

    @Value("${crawling.timeout:10000}")
    private int timeout;

    @Value("${crawling.rate-limit-ms:1000}")
    private long rateLimitMs;

    @Value("${crawling.max-articles-per-source:10}")
    private int maxArticlesPerSource;

    /**
     * 단일 뉴스 소스에서 기사를 크롤링한다.
     */
    @Transactional
    public List<CrawledArticle> crawlSource(NewsSource source) {
        List<CrawledArticle> newArticles = new ArrayList<>();

        try {
            // 1. 목록 페이지 가져오기
            Document listPage = Jsoup.connect(source.getBaseUrl())
                    .userAgent(userAgent)
                    .timeout(timeout)
                    .get();

            // 2. 기사 링크 추출 (CSS selector 사용)
            Elements articleLinks = listPage.select(source.getCrawlPattern());
            log.info("[{}] 발견된 기사 링크 수: {}", source.getName(), articleLinks.size());

            int crawledCount = 0;
            for (Element link : articleLinks) {
                if (crawledCount >= maxArticlesPerSource) break;

                String articleUrl = resolveUrl(source.getBaseUrl(), link.attr("href"));
                if (articleUrl == null || articleUrl.isBlank()) continue;

                // 3. 중복 체크
                if (crawledArticleRepository.findByOriginalUrl(articleUrl).isPresent()) {
                    log.debug("[{}] 이미 크롤링된 기사 건너뜀: {}", source.getName(), articleUrl);
                    continue;
                }

                try {
                    // 4. 기사 페이지 파싱
                    Thread.sleep(rateLimitMs); // Rate limiting
                    Document articlePage = Jsoup.connect(articleUrl)
                            .userAgent(userAgent)
                            .timeout(timeout)
                            .get();

                    // 5. 기사 내용 추출
                    String title = extractTitle(articlePage, source.getArticlePattern());
                    String content = extractContent(articlePage, source.getArticlePattern());
                    String author = extractAuthor(articlePage);
                    String thumbnail = extractThumbnail(articlePage);

                    if (title == null || title.isBlank() || content == null || content.isBlank()) {
                        log.warn("[{}] 제목 또는 내용 추출 실패: {}", source.getName(), articleUrl);
                        continue;
                    }

                    // 6. 저장
                    CrawledArticle article = CrawledArticle.builder()
                            .newsSource(source)
                            .originalUrl(articleUrl)
                            .originalTitle(title.trim())
                            .originalContent(content.trim())
                            .thumbnailUrl(thumbnail)
                            .author(author)
                            .publishedAt(LocalDateTime.now())
                            .category(source.getCategory())
                            .build();

                    crawledArticleRepository.save(article);
                    newArticles.add(article);
                    crawledCount++;
                    log.info("[{}] 기사 크롤링 완료: {}", source.getName(), title);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("[{}] 크롤링 중단됨", source.getName());
                    break;
                } catch (Exception e) {
                    log.error("[{}] 기사 크롤링 실패 - URL: {}, 오류: {}", source.getName(), articleUrl, e.getMessage());
                }
            }

            // 마지막 크롤링 시간 업데이트
            source.updateLastCrawledAt();
            newsSourceRepository.save(source);

        } catch (Exception e) {
            log.error("[{}] 소스 크롤링 실패: {}", source.getName(), e.getMessage());
        }

        return newArticles;
    }

    /**
     * 활성화된 모든 소스에서 크롤링을 실행한다.
     */
    @Transactional
    public int crawlAllSources() {
        List<NewsSource> enabledSources = newsSourceRepository.findByEnabledTrue();
        log.info("활성화된 뉴스 소스 수: {}", enabledSources.size());

        int totalCrawled = 0;
        for (NewsSource source : enabledSources) {
            List<CrawledArticle> articles = crawlSource(source);
            totalCrawled += articles.size();
        }

        log.info("전체 크롤링 완료. 총 {}건의 새 기사 수집", totalCrawled);
        return totalCrawled;
    }

    /**
     * 상대 URL을 절대 URL로 변환한다.
     */
    private String resolveUrl(String baseUrl, String href) {
        if (href == null || href.isBlank()) return null;
        if (href.startsWith("http://") || href.startsWith("https://")) return href;
        if (href.startsWith("//")) return "https:" + href;

        try {
            java.net.URL base = new java.net.URL(baseUrl);
            return new java.net.URL(base, href).toString();
        } catch (Exception e) {
            log.warn("URL 변환 실패 - base: {}, href: {}", baseUrl, href);
            return null;
        }
    }

    /**
     * 기사 제목을 추출한다.
     */
    private String extractTitle(Document doc, String articlePattern) {
        // articlePattern에서 제목 추출 시도 (패턴 기반)
        Elements titleElements = doc.select(articlePattern + " h1, " + articlePattern + " h2");
        if (!titleElements.isEmpty()) {
            return titleElements.first().text();
        }

        // Fallback: 일반적인 기사 제목 선택자
        Elements fallback = doc.select("h1.article-title, h1.title, .article_title, .news_title, h1");
        if (!fallback.isEmpty()) {
            return fallback.first().text();
        }

        // 최종 Fallback: 페이지 title 태그
        return doc.title();
    }

    /**
     * 기사 본문 내용을 추출한다.
     */
    private String extractContent(Document doc, String articlePattern) {
        Elements contentElements = doc.select(articlePattern);
        if (!contentElements.isEmpty()) {
            Element contentEl = contentElements.first();
            // 광고, 네비게이션 등 불필요한 요소 제거
            contentEl.select("script, style, iframe, .ad, .advertisement, .banner, nav, footer").remove();
            return contentEl.text();
        }

        // Fallback: 일반적인 본문 선택자
        Elements fallback = doc.select(".article-body, .article_content, .news_content, .view_cont, article");
        if (!fallback.isEmpty()) {
            Element el = fallback.first();
            el.select("script, style, iframe, .ad, .advertisement, .banner, nav, footer").remove();
            return el.text();
        }

        return null;
    }

    /**
     * 기사 작성자를 추출한다.
     */
    private String extractAuthor(Document doc) {
        Elements authorElements = doc.select(".author, .writer, .reporter, .byline, [rel=author]");
        if (!authorElements.isEmpty()) {
            return authorElements.first().text();
        }
        return null;
    }

    /**
     * 기사 대표 이미지를 추출한다.
     */
    private String extractThumbnail(Document doc) {
        // Open Graph 이미지
        Element ogImage = doc.selectFirst("meta[property=og:image]");
        if (ogImage != null) {
            String content = ogImage.attr("content");
            if (!content.isBlank()) return content;
        }

        // 본문 내 첫 번째 이미지
        Element firstImg = doc.selectFirst("article img, .article-body img, .article_content img");
        if (firstImg != null) {
            String src = firstImg.attr("src");
            if (!src.isBlank()) return src;
        }

        return null;
    }
}
