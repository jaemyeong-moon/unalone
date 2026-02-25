package com.project.api.service;

import com.project.api.domain.CheckIn;
import com.project.api.domain.Profile;
import com.project.api.domain.User;
import com.project.api.kafka.producer.ApiEventProducer;
import com.project.api.repository.CheckInRepository;
import com.project.api.repository.ProfileRepository;
import com.project.api.repository.UserRepository;
import com.project.common.event.CheckInMissedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInScheduler {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CheckInRepository checkInRepository;
    private final ApiEventProducer eventProducer;

    @Scheduled(fixedRate = 3600000)
    @Transactional(readOnly = true)
    public void detectMissedCheckIns() {
        log.info("미응답 체크인 감지 스케줄러 실행");

        List<User> activeUsers = userRepository.findAll().stream()
                .filter(u -> u.getStatus() == User.UserStatus.ACTIVE)
                .toList();

        for (User user : activeUsers) {
            try {
                checkUserMissedCheckIn(user);
            } catch (Exception e) {
                log.error("사용자 {} 체크인 감지 중 오류: {}", user.getId(), e.getMessage());
            }
        }
    }

    private void checkUserMissedCheckIn(User user) {
        Optional<Profile> profileOpt = profileRepository.findByUserId(user.getId());
        if (profileOpt.isEmpty()) return;

        Profile profile = profileOpt.get();
        int intervalHours = profile.getCheckIntervalHours();

        Optional<CheckIn> lastCheckIn = checkInRepository.findLatestByUserId(user.getId());

        LocalDateTime expectedAt;
        boolean missed;

        if (lastCheckIn.isEmpty()) {
            expectedAt = user.getCreatedAt().plusHours(intervalHours);
            missed = LocalDateTime.now().isAfter(expectedAt);
        } else {
            expectedAt = lastCheckIn.get().getCheckedAt().plusHours(intervalHours);
            missed = LocalDateTime.now().isAfter(expectedAt);
        }

        if (missed) {
            long missedCount = checkInRepository.countByUserIdAndCheckedAtAfter(
                    user.getId(),
                    LocalDateTime.now().minusDays(7)
            );

            log.warn("미응답 감지: userId={}, expectedAt={}, missedCount={}", user.getId(), expectedAt, missedCount);

            try {
                eventProducer.publishEvent("checkin-events",
                        new CheckInMissedEvent(user.getId(), expectedAt, (int) missedCount));
            } catch (Exception e) {
                log.error("미응답 이벤트 발행 실패: {}", e.getMessage());
            }
        }
    }
}
