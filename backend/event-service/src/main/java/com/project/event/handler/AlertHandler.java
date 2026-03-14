package com.project.event.handler;

import com.project.common.event.AlertCreatedEvent;
import com.project.common.event.EventPublisher;
import com.project.event.domain.Alert;
import com.project.event.domain.AlertLevel;
import com.project.event.domain.AlertStatus;
import com.project.event.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 체크인 미응답 알림 생성 및 해결 처리를 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertHandler {

    private static final String ALERT_TOPIC = "alert-events";

    private final AlertRepository alertRepository;
    private final EventPublisher eventPublisher;

    /**
     * 체크인 미응답 이벤트를 처리하여 필요 시 알림을 생성합니다.
     * 이미 ACTIVE 상태의 알림이 존재하면 중복 생성을 방지합니다.
     *
     * @param userId      미응답 사용자 ID
     * @param missedCount 누적 미응답 횟수
     */
    public void handleCheckInMissed(Long userId, int missedCount) {
        if (alertRepository.countByUserIdAndStatus(userId, AlertStatus.ACTIVE) > 0) {
            log.info("Active alert already exists for user {}, skipping", userId);
            return;
        }

        AlertLevel level = AlertLevel.from(missedCount);
        Alert alert = Alert.builder()
                .userId(userId)
                .level(level)
                .message("안부 체크 미응답 감지 (미응답 " + missedCount + "회)")
                .status(AlertStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        alertRepository.save(alert);
        log.warn("Alert created: userId={}, level={}, alertId={}", userId, level, alert.getId());

        publishAlertCreatedEvent(alert);
    }

    /**
     * 알림 해결 이벤트를 처리합니다.
     * 알림이 존재하지 않으면 경고 로그를 남기고 무시합니다.
     *
     * @param alertId    해결할 알림 ID
     * @param resolvedBy 처리한 관리자 ID (없으면 null)
     */
    public void handleAlertResolved(String alertId, Long resolvedBy) {
        alertRepository.findById(alertId).ifPresentOrElse(
                alert -> {
                    alertRepository.save(alert.resolve(resolvedBy));
                    log.info("Alert resolved: alertId={}, resolvedBy={}", alertId, resolvedBy);
                },
                () -> log.warn("Alert not found for resolution: alertId={}", alertId)
        );
    }

    private void publishAlertCreatedEvent(Alert alert) {
        try {
            eventPublisher.publish(ALERT_TOPIC,
                    new AlertCreatedEvent(alert.getId(), alert.getUserId(), alert.getLevel().name(), alert.getMessage()));
        } catch (Exception e) {
            log.error("AlertCreatedEvent 발행 실패: alertId={}, error={}", alert.getId(), e.getMessage());
        }
    }
}
