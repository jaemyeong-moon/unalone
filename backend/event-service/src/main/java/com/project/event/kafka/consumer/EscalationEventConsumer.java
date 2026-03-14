package com.project.event.kafka.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.event.domain.Alert;
import com.project.event.domain.AlertLevel;
import com.project.event.domain.AlertStatus;
import com.project.event.handler.EventHandler;
import com.project.event.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 에스컬레이션 이벤트 컨슈머.
 * escalation-events, anomaly-events 토픽을 구독하여 MongoDB에 로깅하고,
 * 에스컬레이션 레벨에 따라 Alert를 생성합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EscalationEventConsumer {

    private final EventHandler eventHandler;
    private final AlertRepository alertRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {"${kafka.topics.escalation-events:escalation-events}", "${kafka.topics.anomaly-events:anomaly-events}"},
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String message) {
        log.debug("Escalation/Anomaly event received: {}", message);
        try {
            // 모든 이벤트를 MongoDB에 로깅
            eventHandler.handleEvent(message);

            // 에스컬레이션 이벤트에 대한 추가 처리
            processEscalationEvent(message);

        } catch (Exception e) {
            log.error("Failed to consume escalation/anomaly event: {}", e.getMessage(), e);
        }
    }

    /**
     * 에스컬레이션 이벤트를 분석하여 Alert를 생성하거나 해제합니다.
     */
    private void processEscalationEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.path("eventType").asText();
            Long userId = root.path("userId").asLong();
            Long escalationId = root.path("escalationId").asLong();
            String currentLevel = root.path("currentLevel").asText();
            String previousLevel = root.path("previousLevel").asText(null);

            switch (eventType) {
                case "ESCALATION_CREATED" -> {
                    log.info("에스컬레이션 생성: userId={}, level={}", userId, currentLevel);
                    createAlertFromEscalation(userId, escalationId, currentLevel);
                }
                case "ESCALATION_LEVEL_CHANGED" -> {
                    log.warn("에스컬레이션 단계 변경: userId={}, {} -> {}", userId, previousLevel, currentLevel);
                    createAlertFromEscalation(userId, escalationId, currentLevel);
                }
                case "ESCALATION_RESOLVED" -> {
                    log.info("에스컬레이션 해제: userId={}, escalationId={}", userId, escalationId);
                    resolveAlertsForUser(userId);
                }
                default -> log.debug("에스컬레이션 이벤트 저장 완료: type={}", eventType);
            }
        } catch (Exception e) {
            log.error("에스컬레이션 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 에스컬레이션 레벨에 따라 Alert를 생성합니다.
     * WARNING 이상부터 Alert를 생성합니다.
     */
    private void createAlertFromEscalation(Long userId, Long escalationId, String level) {
        // REMINDER는 Alert 생성하지 않음
        if ("REMINDER".equals(level)) {
            return;
        }

        AlertLevel alertLevel = mapEscalationToAlertLevel(level);
        if (alertLevel == null) {
            return;
        }

        // 기존 ACTIVE 알림이 있으면 중복 생성 방지
        if (alertRepository.countByUserIdAndStatus(userId, AlertStatus.ACTIVE) > 0) {
            log.info("기존 활성 알림이 있어 업데이트 없이 유지: userId={}", userId);
            return;
        }

        String message = buildAlertMessage(level);
        Alert alert = Alert.builder()
                .userId(userId)
                .level(alertLevel)
                .message(message)
                .status(AlertStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        alertRepository.save(alert);
        log.warn("에스컬레이션 Alert 생성: userId={}, level={}, alertId={}", userId, alertLevel, alert.getId());
    }

    /**
     * 사용자의 활성 Alert를 모두 해제합니다.
     */
    private void resolveAlertsForUser(Long userId) {
        alertRepository.findByUserIdAndStatus(userId, AlertStatus.ACTIVE)
                .forEach(alert -> {
                    alertRepository.save(alert.resolve(null));
                    log.info("에스컬레이션 해제로 Alert 해결: alertId={}, userId={}", alert.getId(), userId);
                });
    }

    private AlertLevel mapEscalationToAlertLevel(String escalationLevel) {
        return switch (escalationLevel) {
            case "WARNING" -> AlertLevel.WARNING;
            case "DANGER" -> AlertLevel.DANGER;
            case "CRITICAL" -> AlertLevel.CRITICAL;
            default -> null;
        };
    }

    private String buildAlertMessage(String level) {
        return switch (level) {
            case "WARNING" -> "안부 체크인 미응답 1시간 초과 (WARNING 단계)";
            case "DANGER" -> "안부 체크인 미응답 3시간 초과 (DANGER 단계) - 확인이 필요합니다";
            case "CRITICAL" -> "긴급: 안부 체크인 미응답 6시간 초과 (CRITICAL 단계) - 즉시 확인 필요";
            default -> "안부 체크인 미응답 에스컬레이션: " + level;
        };
    }
}
