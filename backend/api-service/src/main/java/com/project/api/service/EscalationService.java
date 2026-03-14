package com.project.api.service;

import com.project.api.domain.*;
import com.project.api.dto.schedule.EscalationResponse;
import com.project.api.exception.BusinessException;
import com.project.api.kafka.producer.ApiEventProducer;
import com.project.api.repository.CheckInRepository;
import com.project.api.repository.CheckInScheduleRepository;
import com.project.api.repository.EscalationRepository;
import com.project.api.repository.UserRepository;
import com.project.common.config.KafkaConfig;
import com.project.common.event.EscalationTriggeredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EscalationService {

    private final EscalationRepository escalationRepository;
    private final CheckInScheduleRepository checkInScheduleRepository;
    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;
    private final ApiEventProducer eventProducer;

    /**
     * 미응답 체크인에 대한 에스컬레이션을 생성하거나 단계를 진행합니다.
     */
    @Transactional
    public void processEscalation(CheckInSchedule schedule) {
        Long userId = schedule.getUser().getId();

        // 마지막 체크인 시간 조회
        Optional<CheckIn> latestCheckIn = checkInRepository.findLatestByUserId(userId);
        LocalDateTime lastCheckedAt = latestCheckIn
                .map(CheckIn::getCheckedAt)
                .orElse(schedule.getUser().getCreatedAt());

        LocalDateTime expectedAt = schedule.getNextCheckInDue();
        if (expectedAt == null || LocalDateTime.now().isBefore(expectedAt)) {
            return; // 아직 체크인 예정 시간이 안 됨
        }

        long elapsedHours = ChronoUnit.HOURS.between(expectedAt, LocalDateTime.now());
        EscalationStage targetStage = EscalationStage.fromElapsedHours(elapsedHours);

        // 기존 활성 에스컬레이션 조회
        Optional<Escalation> existingEscalation = escalationRepository.findByUserIdAndResolvedFalse(userId);

        if (existingEscalation.isPresent()) {
            Escalation escalation = existingEscalation.get();
            EscalationStage currentStage = escalation.getStage();

            // 이미 같은 단계 이상이면 스킵
            if (targetStage.ordinal() <= currentStage.ordinal()) {
                return;
            }

            // 단계 진행
            String previousLevel = currentStage.name();
            escalation.escalateTo(targetStage);
            escalationRepository.save(escalation);

            log.warn("에스컬레이션 단계 진행: userId={}, {} -> {}", userId, previousLevel, targetStage);
            publishEscalationEvent(userId, escalation.getId(), previousLevel, targetStage.name());

        } else {
            // 새 에스컬레이션 생성
            Escalation escalation = Escalation.builder()
                    .user(schedule.getUser())
                    .checkInScheduleId(schedule.getId())
                    .stage(targetStage)
                    .triggeredAt(expectedAt)
                    .build();
            escalationRepository.save(escalation);

            log.warn("에스컬레이션 생성: userId={}, stage={}", userId, targetStage);
            publishEscalationEvent(userId, escalation.getId(), null, targetStage.name());
        }
    }

    /**
     * 체크인 수행 시 활성 에스컬레이션을 해제합니다.
     */
    @Transactional
    public void resolveByCheckIn(Long userId) {
        escalationRepository.findByUserIdAndResolvedFalse(userId)
                .ifPresent(escalation -> {
                    escalation.resolve();
                    escalationRepository.save(escalation);
                    log.info("에스컬레이션 해제 (체크인): userId={}, escalationId={}", userId, escalation.getId());
                });
    }

    @Transactional(readOnly = true)
    public Page<EscalationResponse> getMyEscalations(Long userId, Pageable pageable) {
        return escalationRepository.findByUserIdOrderByTriggeredAtDesc(userId, pageable)
                .map(EscalationResponse::from);
    }

    @Transactional(readOnly = true)
    public EscalationResponse getActiveEscalation(Long userId) {
        return escalationRepository.findByUserIdAndResolvedFalse(userId)
                .map(EscalationResponse::from)
                .orElse(null);
    }

    private void publishEscalationEvent(Long userId, Long escalationId, String previousLevel, String currentLevel) {
        try {
            eventProducer.publishEvent(KafkaConfig.TOPIC_ESCALATION_EVENTS,
                    new EscalationTriggeredEvent(userId, escalationId, previousLevel, currentLevel));
        } catch (Exception e) {
            log.error("에스컬레이션 이벤트 발행 실패: userId={}, error={}", userId, e.getMessage());
        }
    }
}
