package com.project.api.dto.translation;

public record TranslationResult(
        String translatedText,
        boolean success,
        String errorMessage
) {
    public static TranslationResult success(String translatedText) {
        return new TranslationResult(translatedText, true, null);
    }

    public static TranslationResult failure(String errorMessage) {
        return new TranslationResult(null, false, errorMessage);
    }
}
