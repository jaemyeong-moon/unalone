package com.project.admin.service;

import com.project.admin.domain.User;
import com.project.admin.dto.DashboardResponse;
import com.project.admin.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CheckInRepository checkInRepository;
    private final AlertRepository alertRepository;
    private final EscalationRepository escalationRepository;
    private final VolunteerRepository volunteerRepository;
    private final CareMatchRepository careMatchRepository;
    private final HealthJournalRepository healthJournalRepository;

    public DashboardResponse getDashboard() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDate weekAgo = LocalDate.now().minusDays(7);

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(User.UserStatus.ACTIVE);
        long todayCheckIns = checkInRepository.countByCheckedAtAfter(todayStart);

        // Alert stats (MongoDB)
        long activeAlerts = alertRepository.countByStatus("ACTIVE");
        long warningAlerts = alertRepository.countByLevel("WARNING");
        long dangerAlerts = alertRepository.countByLevel("DANGER");
        long criticalAlerts = alertRepository.countByLevel("CRITICAL");

        // New feature stats
        long activeEscalations = escalationRepository.countByResolvedFalse();
        long missedCheckIns = Math.max(0, activeUsers - todayCheckIns);
        long activeVolunteers = volunteerRepository.countByStatus("APPROVED");
        long pendingVolunteers = volunteerRepository.countByStatus("PENDING");
        long activeCareMatches = careMatchRepository.countByStatus("ACTIVE");
        double avgHealthScore = healthJournalRepository.avgHealthScoreSince(weekAgo);
        long criticalHealthAlerts = healthJournalRepository.countCriticalHealthAlertsSince(weekAgo);

        return new DashboardResponse(
                totalUsers,
                activeUsers,
                todayCheckIns,
                activeAlerts,
                warningAlerts,
                dangerAlerts,
                criticalAlerts,
                activeEscalations,
                missedCheckIns,
                activeVolunteers,
                pendingVolunteers,
                activeCareMatches,
                Math.round(avgHealthScore * 10.0) / 10.0,
                criticalHealthAlerts
        );
    }
}
