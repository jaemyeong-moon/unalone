package com.project.api.service;

import com.project.api.domain.HealthJournal;
import com.project.api.domain.User;
import com.project.api.dto.health.*;
import com.project.api.exception.BusinessException;
import com.project.api.kafka.producer.ApiEventProducer;
import com.project.api.repository.HealthJournalRepository;
import com.project.api.repository.UserRepository;
import com.project.common.config.KafkaConfig;
import com.project.common.event.HealthJournalCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthJournalService {

    private final HealthJournalRepository healthJournalRepository;
    private final UserRepository userRepository;
    private final ApiEventProducer eventProducer;

    @Transactional
    public HealthJournalResponse createOrUpdate(Long userId, HealthJournalRequest request) {
        validateRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        LocalDate date = request.date() != null ? request.date() : LocalDate.now();
        String symptomsStr = request.symptoms() != null ? String.join(",", request.symptoms()) : null;
        String medicationsStr = request.medications() != null ? String.join(",", request.medications()) : null;

        int healthScore = calculateHealthScore(request);

        HealthJournal journal = healthJournalRepository.findByUserIdAndDate(userId, date)
                .orElse(null);

        if (journal == null) {
            journal = HealthJournal.builder()
                    .user(user)
                    .date(date)
                    .moodScore(request.moodScore())
                    .sleepHours(request.sleepHours())
                    .sleepQuality(request.sleepQuality())
                    .mealCount(request.mealCount())
                    .mealQuality(request.mealQuality())
                    .exerciseMinutes(request.exerciseMinutes())
                    .exerciseType(request.exerciseType())
                    .symptoms(symptomsStr)
                    .medications(medicationsStr)
                    .painLevel(request.painLevel())
                    .notes(request.notes())
                    .healthScore(healthScore)
                    .build();
        } else {
            journal.update(
                    request.moodScore(),
                    request.sleepHours(),
                    request.sleepQuality(),
                    request.mealCount(),
                    request.mealQuality(),
                    request.exerciseMinutes(),
                    request.exerciseType(),
                    symptomsStr,
                    medicationsStr,
                    request.painLevel(),
                    request.notes(),
                    healthScore
            );
        }

        healthJournalRepository.save(journal);
        log.info("건강 일지 저장: userId={}, date={}, healthScore={}", userId, date, healthScore);

        // Kafka 이벤트 발행
        publishHealthJournalEvent(userId, date, request.moodScore(), healthScore);

        return HealthJournalResponse.from(journal);
    }

    @Transactional(readOnly = true)
    public Page<HealthJournalResponse> getMyJournals(Long userId, Pageable pageable) {
        return healthJournalRepository.findByUserIdOrderByDateDesc(userId, pageable)
                .map(HealthJournalResponse::from);
    }

    @Transactional(readOnly = true)
    public HealthJournalResponse getJournal(Long userId, Long journalId) {
        HealthJournal journal = healthJournalRepository.findById(journalId)
                .orElseThrow(() -> BusinessException.notFound("건강 일지를 찾을 수 없습니다"));

        if (!journal.getUser().getId().equals(userId)) {
            throw BusinessException.unauthorized("본인의 건강 일지만 조회할 수 있습니다");
        }

        return HealthJournalResponse.from(journal);
    }

    @Transactional
    public HealthJournalResponse updateJournal(Long userId, Long journalId, HealthJournalRequest request) {
        validateRequest(request);

        HealthJournal journal = healthJournalRepository.findById(journalId)
                .orElseThrow(() -> BusinessException.notFound("건강 일지를 찾을 수 없습니다"));

        if (!journal.getUser().getId().equals(userId)) {
            throw BusinessException.unauthorized("본인의 건강 일지만 수정할 수 있습니다");
        }

        String symptomsStr = request.symptoms() != null ? String.join(",", request.symptoms()) : null;
        String medicationsStr = request.medications() != null ? String.join(",", request.medications()) : null;
        int healthScore = calculateHealthScore(request);

        journal.update(
                request.moodScore(),
                request.sleepHours(),
                request.sleepQuality(),
                request.mealCount(),
                request.mealQuality(),
                request.exerciseMinutes(),
                request.exerciseType(),
                symptomsStr,
                medicationsStr,
                request.painLevel(),
                request.notes(),
                healthScore
        );

        healthJournalRepository.save(journal);
        return HealthJournalResponse.from(journal);
    }

    @Transactional
    public void deleteJournal(Long userId, Long journalId) {
        HealthJournal journal = healthJournalRepository.findById(journalId)
                .orElseThrow(() -> BusinessException.notFound("건강 일지를 찾을 수 없습니다"));

        if (!journal.getUser().getId().equals(userId)) {
            throw BusinessException.unauthorized("본인의 건강 일지만 삭제할 수 있습니다");
        }

        healthJournalRepository.delete(journal);
        log.info("건강 일지 삭제: userId={}, journalId={}", userId, journalId);
    }

    @Transactional(readOnly = true)
    public HealthTrendResponse getTrends(Long userId, String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(endDate, period);

        long totalEntries = healthJournalRepository.countByUserIdAndDateBetween(userId, startDate, endDate);
        if (totalEntries < 3) {
            throw BusinessException.badRequest("추세를 확인하려면 최소 3일 이상의 기록이 필요합니다");
        }

        List<HealthJournal> journals = healthJournalRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        Map<LocalDate, HealthJournal> journalMap = journals.stream()
                .collect(Collectors.toMap(HealthJournal::getDate, j -> j));

        // 이전 기간 데이터
        long periodDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate prevStartDate = startDate.minusDays(periodDays);
        LocalDate prevEndDate = startDate.minusDays(1);

        // 감정 추세
        HealthTrendResponse.TrendData moodTrend = buildTrendData(journals, journalMap, startDate, endDate,
                j -> j.getMoodScore() != null ? j.getMoodScore().doubleValue() : null,
                healthJournalRepository.findAverageMoodScore(userId, startDate, endDate),
                healthJournalRepository.findAverageMoodScore(userId, prevStartDate, prevEndDate));

        // 건강 점수 추세
        HealthTrendResponse.TrendData healthScoreTrend = buildTrendData(journals, journalMap, startDate, endDate,
                j -> j.getHealthScore() != null ? j.getHealthScore().doubleValue() : null,
                healthJournalRepository.findAverageHealthScore(userId, startDate, endDate),
                healthJournalRepository.findAverageHealthScore(userId, prevStartDate, prevEndDate));

        // 수면 추세
        HealthTrendResponse.TrendData sleepTrend = buildTrendData(journals, journalMap, startDate, endDate,
                j -> j.getSleepHours() != null ? j.getSleepHours().doubleValue() : null,
                journals.stream().filter(j -> j.getSleepHours() != null)
                        .mapToDouble(j -> j.getSleepHours().doubleValue()).average().orElse(0),
                null);

        // 식사 추세
        HealthTrendResponse.TrendData mealTrend = buildTrendData(journals, journalMap, startDate, endDate,
                j -> j.getMealCount() != null ? j.getMealCount().doubleValue() : null,
                journals.stream().filter(j -> j.getMealCount() != null)
                        .mapToDouble(j -> j.getMealCount().doubleValue()).average().orElse(0),
                null);

        // 운동 빈도
        long activeDays = journals.stream()
                .filter(j -> j.getExerciseMinutes() != null && j.getExerciseMinutes() > 0)
                .count();
        HealthTrendResponse.ExerciseFrequency exerciseFrequency = new HealthTrendResponse.ExerciseFrequency(
                totalEntries, activeDays,
                totalEntries > 0 ? (double) activeDays / totalEntries : 0.0
        );

        return new HealthTrendResponse(
                period, startDate, endDate, totalEntries,
                moodTrend, healthScoreTrend, sleepTrend, mealTrend, exerciseFrequency
        );
    }

    @Transactional(readOnly = true)
    public HealthSummaryResponse getSummary(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        HealthJournal todayJournal = healthJournalRepository.findByUserIdAndDate(userId, today)
                .orElse(null);

        Double weeklyMoodAvg = healthJournalRepository.findAverageMoodScore(userId, weekAgo, today);
        Double weeklyHealthAvg = healthJournalRepository.findAverageHealthScore(userId, weekAgo, today);
        long totalEntries = healthJournalRepository.countByUserIdAndDateBetween(userId, weekAgo, today);

        return new HealthSummaryResponse(
                today,
                todayJournal != null ? todayJournal.getMoodScore() : null,
                todayJournal != null ? todayJournal.getHealthScore() : null,
                weeklyMoodAvg,
                weeklyHealthAvg,
                totalEntries
        );
    }

    /**
     * 건강 종합 점수 계산.
     * 감정 30% + 수면질 25% + 식사질 25% + 운동점수 20% = 0~100점
     */
    public int calculateHealthScore(HealthJournalRequest request) {
        double moodComponent = 0;
        double sleepComponent = 0;
        double mealComponent = 0;
        double exerciseComponent = 0;

        int componentCount = 0;

        if (request.moodScore() != null) {
            moodComponent = request.moodScore() * 0.3;
            componentCount++;
        }
        if (request.sleepQuality() != null) {
            sleepComponent = request.sleepQuality() * 0.25;
            componentCount++;
        }
        if (request.mealQuality() != null) {
            mealComponent = request.mealQuality() * 0.25;
            componentCount++;
        }

        // 운동 점수: 미수행 1점, 30분 미만 3점, 30분 이상 5점
        int exerciseScore = 1;
        if (request.exerciseMinutes() != null && request.exerciseMinutes() > 0) {
            exerciseScore = request.exerciseMinutes() >= 30 ? 5 : 3;
        }
        exerciseComponent = exerciseScore * 0.2;

        if (componentCount == 0) {
            return 0;
        }

        // 각 컴포넌트는 5점 만점 기준, 합산 후 20을 곱해 100점 만점으로 변환
        double totalScore = (moodComponent + sleepComponent + mealComponent + exerciseComponent) * 20;
        return (int) Math.round(Math.min(100, Math.max(0, totalScore)));
    }

    private void validateRequest(HealthJournalRequest request) {
        if (request.moodScore() != null && (request.moodScore() < 1 || request.moodScore() > 5)) {
            throw BusinessException.badRequest("감정 점수는 1~5 사이여야 합니다");
        }
        if (request.sleepQuality() != null && (request.sleepQuality() < 1 || request.sleepQuality() > 5)) {
            throw BusinessException.badRequest("수면 질 점수는 1~5 사이여야 합니다");
        }
        if (request.mealQuality() != null && (request.mealQuality() < 1 || request.mealQuality() > 5)) {
            throw BusinessException.badRequest("식사 질 점수는 1~5 사이여야 합니다");
        }
        if (request.painLevel() != null && (request.painLevel() < 0 || request.painLevel() > 10)) {
            throw BusinessException.badRequest("통증 레벨은 0~10 사이여야 합니다");
        }
        if (request.sleepHours() != null && (request.sleepHours().compareTo(BigDecimal.ZERO) < 0
                || request.sleepHours().compareTo(BigDecimal.valueOf(24)) > 0)) {
            throw BusinessException.badRequest("수면 시간은 0~24시간 사이여야 합니다");
        }
    }

    private LocalDate calculateStartDate(LocalDate endDate, String period) {
        return switch (period.toUpperCase()) {
            case "WEEKLY" -> endDate.minusWeeks(1);
            case "MONTHLY" -> endDate.minusMonths(1);
            case "TWO_WEEKS" -> endDate.minusWeeks(2);
            case "THREE_MONTHS" -> endDate.minusMonths(3);
            default -> endDate.minusWeeks(1);
        };
    }

    private interface ScoreExtractor {
        Double extract(HealthJournal journal);
    }

    private HealthTrendResponse.TrendData buildTrendData(
            List<HealthJournal> journals,
            Map<LocalDate, HealthJournal> journalMap,
            LocalDate startDate, LocalDate endDate,
            ScoreExtractor extractor,
            Double currentAvg, Double prevAvg) {

        List<MoodTrendItem> data = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            HealthJournal j = journalMap.get(d);
            Double score = j != null ? extractor.extract(j) : null;
            data.add(new MoodTrendItem(d, score));
        }

        String trend = "STABLE";
        Double changePercent = 0.0;
        if (currentAvg != null && prevAvg != null && prevAvg > 0) {
            changePercent = ((currentAvg - prevAvg) / prevAvg) * 100;
            if (changePercent > 5) trend = "IMPROVING";
            else if (changePercent < -5) trend = "DECLINING";
        }

        return new HealthTrendResponse.TrendData(data, currentAvg, trend, changePercent);
    }

    private void publishHealthJournalEvent(Long userId, LocalDate date, Integer moodScore, Integer healthScore) {
        try {
            eventProducer.publishEvent(KafkaConfig.TOPIC_HEALTH_JOURNAL_EVENTS,
                    new HealthJournalCreatedEvent(userId, date, moodScore, healthScore));
        } catch (Exception e) {
            log.warn("건강 일지 이벤트 발행 실패: userId={}, error={}", userId, e.getMessage());
        }
    }
}
