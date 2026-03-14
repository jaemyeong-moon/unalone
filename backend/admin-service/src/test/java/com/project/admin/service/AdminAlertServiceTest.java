package com.project.admin.service;

import com.project.admin.domain.Alert;
import com.project.admin.domain.User;
import com.project.admin.dto.AlertResponse;
import com.project.admin.exception.ResourceNotFoundException;
import com.project.admin.kafka.producer.AdminEventProducer;
import com.project.admin.repository.AlertRepository;
import com.project.admin.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminAlertService 테스트")
class AdminAlertServiceTest {

    @InjectMocks
    private AdminAlertService adminAlertService;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminEventProducer eventProducer;

    private User createMockUser(Long id, String name) {
        User user = mock(User.class);
        given(user.getId()).willReturn(id);
        given(user.getName()).willReturn(name);
        return user;
    }

    @Nested
    @DisplayName("getAlerts 메서드")
    class GetAlerts {

        @Test
        @DisplayName("전체 알림 조회 성공")
        void getAlerts_all() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            Alert alert = Alert.builder()
                    .id("alert-1").userId(1L).level("WARNING")
                    .message("미응답 감지").status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .build();
            Page<Alert> alertPage = new PageImpl<>(List.of(alert), pageable, 1);

            given(alertRepository.findAll(pageable)).willReturn(alertPage);
            given(userRepository.findById(1L)).willReturn(Optional.of(createMockUser(1L, "홍길동")));

            // Act
            Page<AlertResponse> result = adminAlertService.getAlerts(null, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).userName()).isEqualTo("홍길동");
            assertThat(result.getContent().get(0).level()).isEqualTo("WARNING");
        }

        @Test
        @DisplayName("상태 필터로 알림 조회 성공")
        void getAlerts_withStatusFilter() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            Alert alert = Alert.builder()
                    .id("alert-1").userId(1L).level("DANGER")
                    .message("미응답 감지").status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .build();
            Page<Alert> alertPage = new PageImpl<>(List.of(alert), pageable, 1);

            given(alertRepository.findByStatusOrderByCreatedAtDesc("ACTIVE", pageable)).willReturn(alertPage);
            given(userRepository.findById(1L)).willReturn(Optional.of(createMockUser(1L, "홍길동")));

            // Act
            Page<AlertResponse> result = adminAlertService.getAlerts("ACTIVE", pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            verify(alertRepository).findByStatusOrderByCreatedAtDesc("ACTIVE", pageable);
        }

        @Test
        @DisplayName("사용자를 찾을 수 없을 때 userName이 'Unknown'으로 설정")
        void getAlerts_unknownUser() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            Alert alert = Alert.builder()
                    .id("alert-1").userId(999L).level("WARNING")
                    .message("미응답 감지").status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .build();
            Page<Alert> alertPage = new PageImpl<>(List.of(alert), pageable, 1);

            given(alertRepository.findAll(pageable)).willReturn(alertPage);
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // Act
            Page<AlertResponse> result = adminAlertService.getAlerts(null, pageable);

            // Assert
            assertThat(result.getContent().get(0).userName()).isEqualTo("Unknown");
        }
    }

    @Nested
    @DisplayName("resolveAlert 메서드")
    class ResolveAlert {

        @Test
        @DisplayName("알림 해결 성공")
        void resolveAlert_success() {
            // Arrange
            Alert alert = Alert.builder()
                    .id("alert-1").userId(1L).level("WARNING")
                    .message("미응답 감지").status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .build();
            given(alertRepository.findById("alert-1")).willReturn(Optional.of(alert));
            given(alertRepository.save(any(Alert.class))).willAnswer(inv -> inv.getArgument(0));

            // Act
            adminAlertService.resolveAlert("alert-1", 1L);

            // Assert
            verify(alertRepository).save(any(Alert.class));
            verify(eventProducer).publishEvent(anyString(), any());
        }

        @Test
        @DisplayName("존재하지 않는 알림 해결 시 ResourceNotFoundException")
        void resolveAlert_notFound() {
            // Arrange
            given(alertRepository.findById("nonexistent")).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> adminAlertService.resolveAlert("nonexistent", 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("알림을 찾을 수 없습니다");
        }
    }
}
