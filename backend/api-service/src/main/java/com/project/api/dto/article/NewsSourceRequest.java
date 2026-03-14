package com.project.api.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NewsSourceRequest(
        @NotBlank(message = "이름은 필수입니다")
        String name,

        @NotBlank(message = "URL은 필수입니다")
        String baseUrl,

        @NotBlank(message = "크롤링 패턴은 필수입니다")
        String crawlPattern,

        @NotBlank(message = "기사 패턴은 필수입니다")
        String articlePattern,

        @NotNull(message = "카테고리는 필수입니다")
        String category,

        Boolean enabled
) {
}
