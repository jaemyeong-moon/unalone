package com.project.admin.service;

import com.project.admin.domain.User;
import com.project.admin.dto.DashboardResponse;
import com.project.admin.repository.AlertRepository;
import com.project.admin.repository.CheckInRepository;
import com.project.admin.repository.UserRepository;
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

    public DashboardResponse getDashboard() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        return new DashboardResponse(
                userRepository.count(),
                userRepository.countByStatus(User.UserStatus.ACTIVE),
                checkInRepository.countByCheckedAtAfter(todayStart),
                alertRepository.countByStatus("ACTIVE"),
                alertRepository.countByLevel("WARNING"),
                alertRepository.countByLevel("DANGER"),
                alertRepository.countByLevel("CRITICAL")
        );
    }
}
