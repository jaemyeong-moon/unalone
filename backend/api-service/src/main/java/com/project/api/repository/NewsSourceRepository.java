package com.project.api.repository;

import com.project.api.domain.ArticleCategory;
import com.project.api.domain.NewsSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsSourceRepository extends JpaRepository<NewsSource, Long> {

    List<NewsSource> findByEnabledTrue();

    List<NewsSource> findByCategory(ArticleCategory category);
}
