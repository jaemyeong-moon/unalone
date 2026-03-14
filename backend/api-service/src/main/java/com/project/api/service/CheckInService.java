package com.project.api.service;

import com.project.api.domain.CheckIn;
import com.project.api.domain.User;
import com.project.api.dto.checkin.CheckInRequest;
import com.project.api.dto.checkin.CheckInResponse;
import com.project.api.exception.BusinessException;
import com.project.api.kafka.producer.ApiEventProducer;
import com.project.api.repository.CheckInRepository;
import com.project.api.repository.CheckInScheduleRepository;
import com.project.api.repository.UserRepository;
import com.project.common.event.CheckInCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final UserRepository userRepository;
    private final ApiEventProducer eventProducer;
    private final EscalationService escalationService;
    private final CheckInScheduleRepository checkInScheduleRepository;
    private final CheckInScheduleService checkInScheduleService;

    @Transactional
    public CheckInResponse checkIn(Long userId, CheckInRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        CheckIn checkIn = CheckIn.builder()
                .user(user)
                .status(CheckIn.CheckInStatus.CHECKED)
                .message(request.message())
                .moodScore(request.moodScore())
                .build();
        checkInRepository.save(checkIn);

        // 활성 에스컬레이션 해제
        try {
            escalationService.resolveByCheckIn(userId);
        } catch (Exception e) {
            log.warn("에스컬레이션 해제 실패: {}", e.getMessage());
        }

        // 다음 체크인 예정 시간 업데이트
        try {
            checkInScheduleRepository.findByUserId(userId).ifPresent(schedule -> {
                LocalDateTime nextDue = checkInScheduleService.calculateNextCheckInDue(schedule);
                schedule.updateNextCheckInDue(nextDue);
                checkInScheduleRepository.save(schedule);
            });
        } catch (Exception e) {
            log.warn("다음 체크인 시간 업데이트 실패: {}", e.getMessage());
        }

        try {
            eventProducer.publishEvent("checkin-events",
                    new CheckInCompletedEvent(userId, request.message()));
        } catch (Exception e) {
            log.warn("체크인 이벤트 발행 실패: {}", e.getMessage());
        }

        return CheckInResponse.from(checkIn);
    }

    @Transactional(readOnly = true)
    public Page<CheckInResponse> getMyCheckIns(Long userId, Pageable pageable) {
        return checkInRepository.findByUserIdOrderByCheckedAtDesc(userId, pageable)
                .map(CheckInResponse::from);
    }

    @Transactional(readOnly = true)
    public CheckInResponse getLatestCheckIn(Long userId) {
        return checkInRepository.findLatestByUserId(userId)
                .map(CheckInResponse::from)
                .orElseThrow(() -> BusinessException.notFound("체크인 기록이 없습니다"));
    }
}
