package com.project.event.handler;

import com.project.common.event.EventPublisher;
import com.project.event.domain.Alert;
import com.project.event.domain.AlertLevel;
import com.project.event.domain.AlertStatus;
import com.project.event.repository.AlertRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertHandler 테스트")
class AlertHandlerTest {

    @InjectMocks
    private AlertHandler alertHandler;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Nested
    @DisplayName("handleCheckInMissed 메서드")
    class HandleCheckInMissed {

        @Test
        @DisplayName("1회 미응답 시 WARNING 레벨 알림 생성")
        void handleCheckInMissed_firstMiss_warning() {
            // Arrange
            Long userId = 1L;
            given(alertRepository.countByUserIdAndStatus(userId, AlertStatus.ACTIVE)).willReturn(0L);
            given(alertRepository.save(any(Alert.class))).willAnswer(inv -> inv.getArgument(0));

            // Act
            alertHandler.handleCheckInMissed(userId, 1);

            // Assert
            ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
            verify(alertRepository).save(captor.capture());

            Alert savedAlert = captor.getValue();
            assertThat(savedAlert.getUserId()).isEqualTo(userId);
            assertThat(savedAlert.getLevel()).isEqualTo(AlertLevel.WARNING);
            assertThat(savedAlert.getStatus()).isEqualTo(AlertStatus.ACTIVE);
            assertThat(savedAlert.getMessage()).contains("미응답 1회");

            verify(eventPublisher).publish(anyString(), any());
        }

        @Test
        @DisplayName("2회 미응답 시 DANGER 레벨 알림 생성")
        void handleCheckInMissed_secondMiss_danger() {
            // Arrange
            Long userId = 1L;
            given(alertRepository.countByUserIdAndStatus(userId, AlertStatus.ACTIVE)).willReturn(0L);
            given(alertRepository.save(any(Alert.class))).willAnswer(inv -> inv.getArgument(0));

            // Act
            alertHandler.handleCheckInMissed(userId, 2);

            // Assert
            ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
            verify(alertRepository).save(captor.capture());
            assertThat(captor.getValue().getLevel()).isEqualTo(AlertLevel.DANGER);
        }

        @Test
        @DisplayName("3회 이상 미응답 시 CRITICAL 레벨 알림 생성")
        void handleCheckInMissed_thirdMiss_critical() {
            // Arrange
            Long userId = 1L;
            given(alertRepository.countByUserIdAndStatus(userId, AlertStatus.ACTIVE)).willReturn(0L);
            given(alertRepository.save(any(Alert.class))).willAnswer(inv -> inv.getArgument(0));

            // Act
            alertHandler.handleCheckInMissed(userId, 3);

            // Assert
            ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
            verify(alertRepository).save(captor.capture());
            assertThat(captor.getValue().getLevel()).isEqualTo(AlertLevel.CRITICAL);
        }

        @Test
        @DisplayName("이미 ACTIVE 알림이 있으면 중복 생성하지 않음")
        void handleCheckInMissed_existingAlert_skips() {
            // Arrange
            Long userId = 1L;
            given(alertRepository.countByUserIdAndStatus(userId, AlertStatus.ACTIVE)).willReturn(1L);

            // Act
            alertHandler.handleCheckInMissed(userId, 2);

            // Assert
            verify(alertRepository, never()).save(any());
            verify(eventPublisher, never()).publish(anyString(), any());
        }
    }

    @Nested
    @DisplayName("handleAlertResolved 메서드")
    class HandleAlertResolved {

        @Test
        @DisplayName("알림 해결 성공")
        void handleAlertResolved_success() {
            // Arrange
            String alertId = "alert-1";
            Long resolvedBy = 1L;
            Alert alert = Alert.builder()
                    .id(alertId).userId(1L).level(AlertLevel.WARNING)
                    .message("미응답 감지").status(AlertStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();

            given(alertRepository.findById(alertId)).willReturn(Optional.of(alert));
            given(alertRepository.save(any(Alert.class))).willAnswer(inv -> inv.getArgument(0));

            // Act
            alertHandler.handleAlertResolved(alertId, resolvedBy);

            // Assert
            ArgumentCaptor<Alert> captor = ArgumentCaptor.forClass(Alert.class);
            verify(alertRepository).save(captor.capture());

            Alert resolved = captor.getValue();
            assertThat(resolved.getStatus()).isEqualTo(AlertStatus.RESOLVED);
            assertThat(resolved.getResolvedBy()).isEqualTo(resolvedBy);
            assertThat(resolved.getResolvedAt()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 알림 해결 시 무시 (로그만 남김)")
        void handleAlertResolved_notFound_skips() {
            // Arrange
            given(alertRepository.findById("nonexistent")).willReturn(Optional.empty());

            // Act
            alertHandler.handleAlertResolved("nonexistent", 1L);

            // Assert
            verify(alertRepository, never()).save(any());
        }
    }
}
