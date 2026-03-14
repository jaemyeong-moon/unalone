package com.project.api.service;

import com.project.api.dto.translation.TranslationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 번역 서비스 - placeholder 구현.
 * 실제 운영 환경에서는 Google Translate API, Papago API 등으로 교체하여 사용합니다.
 */
@Service
@Slf4j
public class TranslationService {

    private static final Pattern HANGUL_PATTERN = Pattern.compile("[\\uAC00-\\uD7AF]");
    private static final Pattern JAPANESE_PATTERN = Pattern.compile("[\\u3040-\\u309F\\u30A0-\\u30FF]");
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4E00-\\u9FFF]");

    /**
     * 텍스트의 언어를 감지합니다.
     * 한글, 일본어, 중국어 문자 포함 여부를 기반으로 판별합니다.
     */
    public String detectLanguage(String text) {
        if (text == null || text.isBlank()) {
            return "unknown";
        }

        int hangulCount = countMatches(HANGUL_PATTERN, text);
        int japaneseCount = countMatches(JAPANESE_PATTERN, text);
        int chineseCount = countMatches(CHINESE_PATTERN, text);

        int totalSpecial = hangulCount + japaneseCount + chineseCount;

        if (totalSpecial == 0) {
            return "en";
        }

        if (hangulCount >= japaneseCount && hangulCount >= chineseCount) {
            return "ko";
        }
        if (japaneseCount >= chineseCount) {
            return "ja";
        }
        return "zh";
    }

    /**
     * 텍스트를 번역합니다.
     * 현재는 placeholder 구현으로, 실제 API 호출 대신 간단한 변환을 수행합니다.
     * 운영 환경에서는 Papago/Google Translate API로 교체해야 합니다.
     */
    public TranslationResult translate(String text, String fromLang, String toLang) {
        if (text == null || text.isBlank()) {
            return TranslationResult.failure("Empty text provided");
        }

        if (fromLang.equals(toLang)) {
            return TranslationResult.success(text);
        }

        try {
            // Placeholder 번역: 원문에 번역 표시를 추가
            // 실제 운영 시에는 외부 API 호출로 대체
            String translated = "[Auto-translated from " + fromLang + " to " + toLang + "] " + text;
            log.debug("Placeholder 번역 완료: {} -> {} (길이: {})", fromLang, toLang, text.length());
            return TranslationResult.success(translated);
        } catch (Exception e) {
            log.error("번역 중 오류 발생: {}", e.getMessage(), e);
            return TranslationResult.failure(e.getMessage());
        }
    }

    private int countMatches(Pattern pattern, String text) {
        return (int) pattern.matcher(text).results().count();
    }
}
