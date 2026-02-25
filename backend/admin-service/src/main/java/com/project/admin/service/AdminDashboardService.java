package com.project.admin.service;

import com.project.admin.domain.User;
import com.project.admin.dto.DashboardResponse;
import com.project.admin.repository.AlertRepository;
import com.project.admin.repository.CheckInRepository;
import com.project.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private final UserRepository userRepository;
    private final CheckInRepository checkInRepository;
    private final AlertRepository alertRepository;

    public DashboardResponse getDashboard() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        return DashboardResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByStatus(User.UserStatus.ACTIVE))
                .todayCheckIns(checkInRepository.countByCheckedAtAfter(todayStart))
                .activeAlerts(alertRepository.countByStatus("ACTIVE"))
                .warningAlerts(alertRepository.countByLevel("WARNING"))
                .dangerAlerts(alertRepository.countByLevel("DANGER"))
                .criticalAlerts(alertRepository.countByLevel("CRITICAL"))
                .build();
    }
}
