package com.project.event.handler;

import com.project.event.domain.Alert;
import com.project.event.repository.AlertRepository;
import com.project.common.event.AlertCreatedEvent;
import com.project.common.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertHandler {
    private final AlertRepository alertRepository;
    private final EventPublisher eventPublisher;

    public void handleCheckInMissed(Long userId, int missedCount) {
        String level;
        if (missedCount >= 3) {
            level = "CRITICAL";
        } else if (missedCount >= 2) {
            level = "DANGER";
        } else {
            level = "WARNING";
        }

        // Check if there's already an active alert for this user
        long activeAlerts = alertRepository.countByUserIdAndStatus(userId, "ACTIVE");
        if (activeAlerts > 0) {
            log.info("Active alert already exists for user {}, skipping", userId);
            return;
        }

        Alert alert = Alert.builder()
                .userId(userId)
                .level(level)
                .message("안부 체크 미응답 감지 (미응답 " + missedCount + "회)")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();

        alertRepository.save(alert);
        log.warn("Alert created: userId={}, level={}, alertId={}", userId, level, alert.getId());

        try {
            eventPublisher.publish("alert-events",
                    new AlertCreatedEvent(alert.getId(), userId, level, alert.getMessage()));
        } catch (Exception e) {
            log.error("Alert created event 발행 실패: {}", e.getMessage());
        }
    }

    public void handleAlertResolved(String alertId) {
        alertRepository.findById(alertId).ifPresent(alert -> {
            Alert resolved = Alert.builder()
                    .id(alert.getId()).userId(alert.getUserId()).level(alert.getLevel())
                    .message(alert.getMessage()).status("RESOLVED")
                    .resolvedBy(alert.getResolvedBy()).createdAt(alert.getCreatedAt())
                    .resolvedAt(LocalDateTime.now()).build();
            alertRepository.save(resolved);
            log.info("Alert resolved: {}", alertId);
        });
    }
}
