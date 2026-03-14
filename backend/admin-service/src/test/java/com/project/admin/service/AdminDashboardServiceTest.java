package com.project.admin.service;

import com.project.admin.dto.DashboardResponse;
import com.project.admin.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminDashboardService 테스트")
class AdminDashboardServiceTest {

    @InjectMocks
    private AdminDashboardService dashboardService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private EscalationRepository escalationRepository;

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private CareMatchRepository careMatchRepository;

    @Mock
    private HealthJournalRepository healthJournalRepository;

    @Test
    @DisplayName("대시보드 통계 조회 성공")
    void getDashboard_success() {
        // Arrange
        given(userRepository.count()).willReturn(100L);
        given(userRepository.countByStatus(any())).willReturn(80L);
        given(checkInRepository.countByCheckedAtAfter(any())).willReturn(50L);
        given(alertRepository.countByStatus("ACTIVE")).willReturn(5L);
        given(alertRepository.countByLevel("WARNING")).willReturn(3L);
        given(alertRepository.countByLevel("DANGER")).willReturn(1L);
        given(alertRepository.countByLevel("CRITICAL")).willReturn(1L);
        given(escalationRepository.countByResolvedFalse()).willReturn(2L);
        given(volunteerRepository.countByStatus("APPROVED")).willReturn(10L);
        given(volunteerRepository.countByStatus("PENDING")).willReturn(3L);
        given(careMatchRepository.countByStatus("ACTIVE")).willReturn(5L);
        given(healthJournalRepository.avgHealthScoreSince(any())).willReturn(7.5);
        given(healthJournalRepository.countCriticalHealthAlertsSince(any())).willReturn(2L);

        // Act
        DashboardResponse response = dashboardService.getDashboard();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.totalUsers()).isEqualTo(100L);
        assertThat(response.activeUsers()).isEqualTo(80L);
        assertThat(response.todayCheckIns()).isEqualTo(50L);
        assertThat(response.activeAlerts()).isEqualTo(5L);
        assertThat(response.warningAlerts()).isEqualTo(3L);
        assertThat(response.dangerAlerts()).isEqualTo(1L);
        assertThat(response.criticalAlerts()).isEqualTo(1L);
        assertThat(response.activeEscalations()).isEqualTo(2L);
        assertThat(response.missedCheckIns()).isEqualTo(30L); // 80 - 50
        assertThat(response.activeVolunteers()).isEqualTo(10L);
        assertThat(response.pendingVolunteers()).isEqualTo(3L);
        assertThat(response.activeCareMatches()).isEqualTo(5L);
        assertThat(response.avgHealthScore()).isEqualTo(7.5);
        assertThat(response.criticalHealthAlerts()).isEqualTo(2L);
    }

    @Test
    @DisplayName("체크인 수가 활성 사용자보다 많으면 missedCheckIns는 0")
    void getDashboard_noMissedCheckIns() {
        // Arrange
        given(userRepository.count()).willReturn(10L);
        given(userRepository.countByStatus(any())).willReturn(5L);
        given(checkInRepository.countByCheckedAtAfter(any())).willReturn(10L);
        given(alertRepository.countByStatus("ACTIVE")).willReturn(0L);
        given(alertRepository.countByLevel("WARNING")).willReturn(0L);
        given(alertRepository.countByLevel("DANGER")).willReturn(0L);
        given(alertRepository.countByLevel("CRITICAL")).willReturn(0L);
        given(escalationRepository.countByResolvedFalse()).willReturn(0L);
        given(volunteerRepository.countByStatus("APPROVED")).willReturn(0L);
        given(volunteerRepository.countByStatus("PENDING")).willReturn(0L);
        given(careMatchRepository.countByStatus("ACTIVE")).willReturn(0L);
        given(healthJournalRepository.avgHealthScoreSince(any())).willReturn(0.0);
        given(healthJournalRepository.countCriticalHealthAlertsSince(any())).willReturn(0L);

        // Act
        DashboardResponse response = dashboardService.getDashboard();

        // Assert
        assertThat(response.missedCheckIns()).isEqualTo(0L);
    }
}
