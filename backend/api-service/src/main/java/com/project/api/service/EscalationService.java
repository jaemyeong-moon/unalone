package com.project.api.service;

import com.project.api.domain.*;
import com.project.api.domain.enums.NotificationType;
import com.project.api.dto.schedule.EscalationResponse;
import com.project.api.dto.schedule.EscalationSummaryResponse;
import com.project.api.exception.BusinessException;
import com.project.api.kafka.producer.ApiEventProducer;
import com.project.api.repository.CheckInRepository;
import com.project.api.repository.CheckInScheduleRepository;
import com.project.api.repository.EscalationRepository;
import com.project.api.repository.GuardianRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EscalationService {

    private final EscalationRepository escalationRepository;
    private final CheckInScheduleRepository checkInScheduleRepository;
    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;
    private final GuardianRepository guardianRepository;
    private final ApiEventProducer eventProducer;
    private final NotificationService notificationService;

    /**
     * 미응답 체크인에 대한 에스컬레이션을 생성하거나 단계를 진행합니다.
     * 야간 시간대(quiet hours)에는 CRITICAL을 제외하고 알림을 보내지 않습니다.
     */
    @Transactional
    public void processEscalation(CheckInSchedule schedule) {
        Long userId = schedule.getUser().getId();

        // 일시 중지 상태면 스킵
        if (schedule.isPaused()) {
            return;
        }

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

        // 야간 시간대 체크 (CRITICAL 제외)
        boolean inQuietHours = schedule.isInQuietHours();
        if (inQuietHours && targetStage != EscalationStage.CRITICAL) {
            log.debug("야간 시간대로 에스컬레이션 알림 보류: userId={}, stage={}", userId, targetStage);
            // 에스컬레이션은 생성/진행하되 알림만 보류
        }

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

            // 단계별 알림 전송
            if (!inQuietHours || targetStage == EscalationStage.CRITICAL) {
                sendStageNotifications(userId, escalation, targetStage, previousLevel);
            }

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

            // 단계별 알림 전송
            if (!inQuietHours || targetStage == EscalationStage.CRITICAL) {
                sendStageNotifications(userId, escalation, targetStage, null);
            }
        }
    }

    /**
     * 에스컬레이션 단계에 따른 알림을 전송합니다.
     * REMINDER: 사용자에게만 알림
     * WARNING: 사용자 + 보호자 전원
     * DANGER: 관리자 + 보호자 재알림
     * CRITICAL: 긴급 알림 (관리자 + 보호자 + 사용자)
     */
    private void sendStageNotifications(Long userId, Escalation escalation,
                                         EscalationStage stage, String previousLevel) {
        escalation.incrementNotificationsSent();

        switch (stage) {
            case REMINDER -> {
                notificationService.createNotification(
                        userId,
                        NotificationType.ESCALATION,
                        "안부 확인 리마인더",
                        "체크인 미응답이 감지되었습니다. 안부 체크인을 해주세요.",
                        escalation.getId(),
                        "ESCALATION"
                );
            }
            case WARNING -> {
                // 사용자 알림
                notificationService.createNotification(
                        userId,
                        NotificationType.ESCALATION,
                        "안부 확인 경고",
                        "체크인 미응답으로 에스컬레이션이 WARNING 단계로 진행되었습니다. 즉시 체크인해주세요.",
                        escalation.getId(),
                        "ESCALATION"
                );
                // 보호자 알림
                notifyGuardians(userId, escalation, "WARNING",
                        "돌보는 분의 안부 체크인 미응답이 1시간 이상 지속되고 있습니다. 확인 부탁드립니다.");
            }
            case DANGER -> {
                // 사용자 재알림
                notificationService.createNotification(
                        userId,
                        NotificationType.ESCALATION,
                        "안부 확인 위험",
                        "체크인 미응답이 3시간 이상 지속되고 있습니다. DANGER 단계입니다.",
                        escalation.getId(),
                        "ESCALATION"
                );
                // 보호자 재알림
                notifyGuardians(userId, escalation, "DANGER",
                        "돌보는 분과 연락이 닿지 않고 있습니다 (3시간 이상 미응답). 직접 확인해주세요.");
                // 관리자 알림 (notes에 기록)
                escalation.addNote("DANGER 단계 도달 - 관리자 대시보드에 위험 알림 표시됨");
            }
            case CRITICAL -> {
                // 긴급 알림 - 모든 대상
                notificationService.createNotification(
                        userId,
                        NotificationType.ESCALATION,
                        "긴급 안부 확인",
                        "체크인 미응답이 6시간 이상 지속되고 있습니다. CRITICAL 단계 - 긴급 연락이 필요합니다.",
                        escalation.getId(),
                        "ESCALATION"
                );
                notifyGuardians(userId, escalation, "CRITICAL",
                        "긴급: 돌보는 분과 6시간 이상 연락이 닿지 않습니다. 즉시 확인해주세요. 관리자에게도 알림이 전달되었습니다.");
                escalation.addNote("CRITICAL 단계 도달 - 긴급 연락처(119) 안내 필요");
            }
        }

        escalationRepository.save(escalation);
    }

    /**
     * 보호자에게 알림을 전송합니다.
     * 보호자가 없는 경우 notes에 기록하고, 관리자 알림으로 에스컬레이션합니다.
     */
    private void notifyGuardians(Long userId, Escalation escalation, String level, String message) {
        List<Guardian> guardians = guardianRepository.findByUserId(userId);

        if (guardians.isEmpty()) {
            escalation.addNote("보호자 미등록 - " + level + " 단계에서 보호자 알림 건너뜀, 관리자 알림으로 대체");
            log.warn("보호자 미등록 사용자 에스컬레이션: userId={}, level={}", userId, level);
            return;
        }

        String contacts = guardians.stream()
                .map(g -> g.getName() + "(" + g.getPhone() + ")")
                .collect(Collectors.joining(", "));
        escalation.updateNotifiedContacts(contacts);

        log.info("보호자 알림 전송: userId={}, level={}, guardians={}", userId, level, contacts);
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

                    publishEscalationEvent(userId, escalation.getId(), escalation.getStage().name(), "RESOLVED");
                });
    }

    /**
     * 관리자가 수동으로 에스컬레이션을 해제합니다.
     */
    @Transactional
    public EscalationResponse resolveByAdmin(Long escalationId, String adminIdentifier, String notes) {
        Escalation escalation = escalationRepository.findById(escalationId)
                .orElseThrow(() -> BusinessException.notFound("에스컬레이션을 찾을 수 없습니다: " + escalationId));

        if (escalation.isResolved()) {
            throw BusinessException.badRequest("이미 해결된 에스컬레이션입니다");
        }

        String previousStage = escalation.getStage().name();
        escalation.resolveByAdmin(adminIdentifier, notes);
        escalationRepository.save(escalation);

        log.info("에스컬레이션 해제 (관리자): escalationId={}, admin={}", escalationId, adminIdentifier);
        publishEscalationEvent(escalation.getUser().getId(), escalationId, previousStage, "RESOLVED");

        return EscalationResponse.from(escalation);
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

    /**
     * 모든 활성 에스컬레이션을 페이지네이션으로 조회합니다 (관리자용).
     */
    @Transactional(readOnly = true)
    public Page<EscalationResponse> getActiveEscalations(Pageable pageable) {
        return escalationRepository.findAllActiveEscalationsPaged(pageable)
                .map(EscalationResponse::from);
    }

    /**
     * 특정 단계의 활성 에스컬레이션을 조회합니다 (관리자용).
     */
    @Transactional(readOnly = true)
    public Page<EscalationResponse> getActiveEscalationsByStage(EscalationStage stage, Pageable pageable) {
        return escalationRepository.findAllActiveEscalationsByStage(stage, pageable)
                .map(EscalationResponse::from);
    }

    /**
     * 에스컬레이션 요약 정보를 반환합니다 (관리자 대시보드용).
     */
    @Transactional(readOnly = true)
    public EscalationSummaryResponse getEscalationSummary() {
        long reminderCount = escalationRepository.countByStageAndResolvedFalse(EscalationStage.REMINDER);
        long warningCount = escalationRepository.countByStageAndResolvedFalse(EscalationStage.WARNING);
        long dangerCount = escalationRepository.countByStageAndResolvedFalse(EscalationStage.DANGER);
        long criticalCount = escalationRepository.countByStageAndResolvedFalse(EscalationStage.CRITICAL);

        return EscalationSummaryResponse.of(reminderCount, warningCount, dangerCount, criticalCount);
    }

    /**
     * 사용자의 에스컬레이션 이력을 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<EscalationResponse> getUserEscalationHistory(Long userId, Pageable pageable) {
        return escalationRepository.findByUserIdOrderByTriggeredAtDesc(userId, pageable)
                .map(EscalationResponse::from);
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
