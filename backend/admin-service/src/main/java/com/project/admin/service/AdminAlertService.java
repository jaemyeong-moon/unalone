package com.project.admin.service;

import com.project.admin.domain.Alert;
import com.project.admin.domain.User;
import com.project.admin.dto.AlertResponse;
import com.project.admin.kafka.producer.AdminEventProducer;
import com.project.admin.repository.AlertRepository;
import com.project.admin.repository.UserRepository;
import com.project.common.event.AlertResolvedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAlertService {
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final AdminEventProducer eventProducer;

    public Page<AlertResponse> getAlerts(String status, Pageable pageable) {
        Page<Alert> alerts;
        if (status != null && !status.isEmpty()) {
            alerts = alertRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            alerts = alertRepository.findAll(pageable);
        }
        return alerts.map(alert -> {
            String userName = userRepository.findById(alert.getUserId())
                    .map(User::getName).orElse("Unknown");
            return AlertResponse.from(alert, userName);
        });
    }

    public void resolveAlert(String alertId, Long adminUserId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다"));
        Alert resolved = Alert.builder()
                .id(alert.getId()).userId(alert.getUserId()).level(alert.getLevel())
                .message(alert.getMessage()).status("RESOLVED")
                .resolvedBy(adminUserId).createdAt(alert.getCreatedAt())
                .resolvedAt(LocalDateTime.now()).build();
        alertRepository.save(resolved);
        try {
            eventProducer.publishEvent("alert-events", new AlertResolvedEvent(alertId, adminUserId));
        } catch (Exception e) {
            log.warn("Alert resolved event 발행 실패: {}", e.getMessage());
        }
    }
}
