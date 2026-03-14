package com.project.api.service;

import com.project.api.domain.CommunityPost;
import com.project.api.domain.QualityGrade;
import com.project.api.domain.User;
import com.project.api.dto.quality.QualityScoreResult;
import com.project.api.repository.CommunityPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 게시글 콘텐츠 품질 점수를 산출하는 서비스.
 * 콘텐츠 길이, 제목 품질, 주제 관련성, 구조, 독창성, 작성자 신뢰도를 종합 평가합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ContentQualityService {

    private final CommunityPostRepository postRepository;

    // 도메인 관련 키워드 (고독사 방지, 돌봄, 건강, 복지 관련)
    private static final List<String> DOMAIN_KEYWORDS = List.of(
            "건강", "돌봄", "안전", "운동", "식사", "수면", "복지",
            "1인가구", "고독사", "안부", "이웃", "봉사", "병원", "약",
            "체크인", "산책", "취미", "요리", "정보", "경험", "도움",
            "생활", "건강관리", "의료", "복약", "간호", "방문", "돌봄서비스",
            "응급", "119", "안심", "케어", "상담", "심리", "우울",
            "외로움", "교류", "모임", "프로그램", "센터", "지원"
    );

    // 스팸 키워드
    private static final List<String> SPAM_KEYWORDS = List.of(
            "광고", "홍보", "할인", "무료체험", "클릭", "당첨", "수익",
            "투자", "대출", "카지노", "도박", "성인"
    );

    // 반복 문자 패턴 (ㅋㅋㅋ, ㅎㅎㅎ, ㅠㅠㅠ 등)
    private static final Pattern REPEATED_CHAR_PATTERN = Pattern.compile("(.)\\1{3,}");

    // URL 패턴
    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+");

    /**
     * 게시글의 품질 점수를 계산합니다 (0-100).
     */
    public QualityScoreResult scorePost(CommunityPost post) {
        Map<String, Integer> breakdown = new LinkedHashMap<>();

        // 스팸 게시글 조기 감지
        if (isSpamContent(post.getContent())) {
            breakdown.put("contentLength", 0);
            breakdown.put("titleQuality", 0);
            breakdown.put("relevance", 0);
            breakdown.put("structure", 0);
            breakdown.put("originality", 0);
            breakdown.put("authorCredibility", 0);
            return new QualityScoreResult(5, QualityGrade.SPAM, breakdown);
        }

        // 1. 콘텐츠 길이 점수 (최대 20점)
        int contentLengthScore = calculateContentLengthScore(post.getContent());
        breakdown.put("contentLength", contentLengthScore);

        // 2. 제목 품질 점수 (최대 15점)
        int titleQualityScore = calculateTitleQualityScore(post.getTitle());
        breakdown.put("titleQuality", titleQualityScore);

        // 3. 주제 관련성 점수 (최대 25점)
        int relevanceScore = calculateRelevanceScore(post.getContent());
        breakdown.put("relevance", Math.min(relevanceScore, 25));

        // 4. 구조 점수 (최대 15점)
        int structureScore = calculateStructureScore(post.getContent());
        breakdown.put("structure", Math.min(structureScore, 15));

        // 5. 독창성 점수 (최대 10점)
        int originalityScore = calculateOriginalityScore(post.getContent());
        breakdown.put("originality", Math.min(originalityScore, 10));

        // 6. 작성자 신뢰도 점수 (최대 15점)
        int credibilityScore = calculateAuthorCredibility(post.getUser());
        breakdown.put("authorCredibility", Math.min(credibilityScore, 15));

        int totalScore = breakdown.values().stream().mapToInt(Integer::intValue).sum();
        totalScore = Math.max(0, Math.min(100, totalScore));
        QualityGrade grade = QualityGrade.fromScore(totalScore);

        return new QualityScoreResult(totalScore, grade, breakdown);
    }

    /**
     * 콘텐츠 길이 점수 (최대 20점)
     */
    private int calculateContentLengthScore(String content) {
        int length = content.length();
        if (length >= 500) return 20;
        if (length >= 200) return 15;
        if (length >= 100) return 10;
        if (length >= 50) return 5;
        return 0;
    }

    /**
     * 제목 품질 점수 (최대 15점)
     */
    private int calculateTitleQualityScore(String title) {
        int titleLen = title.length();
        boolean hasKeywords = containsRelevantKeywords(title);

        if (titleLen >= 10 && titleLen <= 50) {
            return hasKeywords ? 15 : 10;
        }
        if (titleLen >= 5) {
            return 5;
        }
        return 0;
    }

    /**
     * 주제 관련성 점수 (최대 25점) - 도메인 키워드 매칭 기반
     */
    private int calculateRelevanceScore(String content) {
        long matchCount = DOMAIN_KEYWORDS.stream()
                .filter(content::contains)
                .count();

        if (matchCount >= 5) return 25;
        if (matchCount >= 3) return 20;
        if (matchCount >= 2) return 15;
        if (matchCount >= 1) return 10;
        return 3; // 키워드가 없어도 최소 점수
    }

    /**
     * 구조 점수 (최대 15점) - 문단, 문장 길이, 특수문자 비율
     */
    private int calculateStructureScore(String content) {
        int score = 0;

        // 문단 수 (줄바꿈 기준)
        String[] paragraphs = content.split("\n");
        long nonEmptyParagraphs = Arrays.stream(paragraphs)
                .filter(p -> !p.isBlank())
                .count();
        if (nonEmptyParagraphs >= 3) {
            score += 5;
        } else if (nonEmptyParagraphs >= 2) {
            score += 3;
        }

        // 평균 문장 길이 (마침표, 물음표, 느낌표 기준)
        String[] sentences = content.split("[.?!。？！]");
        if (sentences.length > 0) {
            double avgLength = Arrays.stream(sentences)
                    .mapToInt(s -> s.trim().length())
                    .average()
                    .orElse(0);
            if (avgLength >= 20 && avgLength <= 80) {
                score += 5;
            } else if (avgLength >= 10) {
                score += 3;
            }
        }

        // 과도한 특수문자 비율 체크
        long specialCharCount = content.chars()
                .filter(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c))
                .count();
        double specialRatio = (double) specialCharCount / content.length();
        if (specialRatio < 0.2) {
            score += 5;
        } else if (specialRatio < 0.4) {
            score += 2;
        }

        return score;
    }

    /**
     * 독창성 점수 (최대 10점) - 반복 문자, 어휘 다양성
     */
    private int calculateOriginalityScore(String content) {
        int score = 0;

        // 반복 문자 체크 (ㅋㅋㅋㅋ, ㅎㅎㅎㅎ 등)
        boolean hasExcessiveRepetition = REPEATED_CHAR_PATTERN.matcher(content).find();
        if (!hasExcessiveRepetition) {
            score += 5;
        }

        // 어휘 다양성 (고유 단어 / 전체 단어 비율)
        String[] words = content.split("\\s+");
        if (words.length > 0) {
            Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
            double diversityRatio = (double) uniqueWords.size() / words.length;
            if (diversityRatio > 0.3) {
                score += 5;
            } else if (diversityRatio > 0.15) {
                score += 2;
            }
        }

        return score;
    }

    /**
     * 작성자 신뢰도 점수 (최대 15점) - 이전 게시글 수와 평균 품질
     */
    private int calculateAuthorCredibility(User user) {
        Double avgScore = postRepository.averageQualityScoreByUserId(user.getId());
        long postCount = postRepository.countByUserId(user.getId());

        if (postCount > 10 && avgScore != null && avgScore > 60) {
            return 15;
        }
        if (postCount > 5 && avgScore != null && avgScore > 50) {
            return 10;
        }
        if (postCount > 0) {
            return 5;
        }
        return 3; // 신규 작성자 기본 점수
    }

    /**
     * 스팸 콘텐츠 여부를 판단합니다.
     */
    private boolean isSpamContent(String content) {
        // 매우 짧은 콘텐츠
        if (content.length() < 10) {
            return true;
        }

        // 스팸 키워드 다수 포함
        long spamKeywordCount = SPAM_KEYWORDS.stream()
                .filter(content::contains)
                .count();
        if (spamKeywordCount >= 3) {
            return true;
        }

        // URL만으로 구성된 콘텐츠
        String withoutUrls = URL_PATTERN.matcher(content).replaceAll("").trim();
        if (withoutUrls.length() < 10 && URL_PATTERN.matcher(content).find()) {
            return true;
        }

        // 반복 문자로만 이루어진 콘텐츠 (ㅋㅋㅋㅋㅋ, ㅎㅎㅎㅎㅎ 등)
        String withoutRepeated = REPEATED_CHAR_PATTERN.matcher(content).replaceAll("").trim();
        if (withoutRepeated.length() < 5) {
            return true;
        }

        return false;
    }

    /**
     * 텍스트에 도메인 관련 키워드가 포함되어 있는지 확인합니다.
     */
    private boolean containsRelevantKeywords(String text) {
        return DOMAIN_KEYWORDS.stream().anyMatch(text::contains);
    }
}
