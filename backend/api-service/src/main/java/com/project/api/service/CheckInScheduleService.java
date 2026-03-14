package com.project.api.service;

import com.project.api.domain.CheckInSchedule;
import com.project.api.domain.User;
import com.project.api.dto.schedule.CheckInScheduleRequest;
import com.project.api.dto.schedule.CheckInScheduleResponse;
import com.project.api.dto.schedule.PauseRequest;
import com.project.api.exception.BusinessException;
import com.project.api.repository.CheckInScheduleRepository;
import com.project.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInScheduleService {

    private final CheckInScheduleRepository checkInScheduleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CheckInScheduleResponse getSchedule(Long userId) {
        CheckInSchedule schedule = checkInScheduleRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("체크인 스케줄이 설정되지 않았습니다"));
        return CheckInScheduleResponse.from(schedule);
    }

    @Transactional
    public CheckInScheduleResponse createOrUpdateSchedule(Long userId, CheckInScheduleRequest request) {
        validateRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다"));

        CheckInSchedule schedule = checkInScheduleRepository.findByUserId(userId)
                .orElse(null);

        String activeDaysStr = request.activeDays() != null
                ? String.join(",", request.activeDays())
                : "MON,TUE,WED,THU,FRI,SAT,SUN";

        if (schedule == null) {
            schedule = CheckInSchedule.builder()
                    .user(user)
                    .intervalHours(request.intervalHours() != null ? request.intervalHours() : 24)
                    .preferredTime(request.preferredTime() != null ? request.preferredTime() : LocalTime.of(9, 0))
                    .activeDays(activeDaysStr)
                    .quietStartTime(request.quietStartTime())
                    .quietEndTime(request.quietEndTime())
                    .pauseUntil(request.pauseUntil())
                    .build();
        } else {
            schedule.updateSchedule(
                    request.intervalHours(),
                    request.preferredTime(),
                    activeDaysStr
            );
            if (request.quietStartTime() != null || request.quietEndTime() != null) {
                schedule.updateQuietHours(request.quietStartTime(), request.quietEndTime());
            }
            if (request.pauseUntil() != null) {
                schedule.pause(request.pauseUntil());
            }
        }

        // 다음 체크인 예정 시간 계산
        LocalDateTime nextDue = calculateNextCheckInDue(schedule);
        schedule.updateNextCheckInDue(nextDue);

        checkInScheduleRepository.save(schedule);
        log.info("체크인 스케줄 업데이트: userId={}, intervalHours={}", userId, schedule.getIntervalHours());

        return CheckInScheduleResponse.from(schedule);
    }

    /**
     * 체크인 스케줄을 일시 중지합니다 (최대 30일).
     */
    @Transactional
    public CheckInScheduleResponse pauseSchedule(Long userId, PauseRequest request) {
        CheckInSchedule schedule = checkInScheduleRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("체크인 스케줄이 설정되지 않았습니다"));

        if (schedule.isPaused()) {
            throw BusinessException.badRequest("이미 일시 중지된 스케줄입니다");
        }

        LocalDate endDate = request.pauseEndDate();
        if (endDate == null) {
            throw BusinessException.badRequest("일시 중지 종료일을 입력해주세요");
        }

        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        if (daysBetween > 30) {
            throw BusinessException.badRequest("임시 비활성 기간은 최대 30일입니다");
        }
        if (daysBetween < 0) {
            throw BusinessException.badRequest("비활성 종료일은 오늘 이후여야 합니다");
        }

        schedule.pause(request.reason(), endDate);

        // 일시 중지 종료 후 다음 체크인 시간 계산
        LocalDateTime nextDue = endDate.atTime(schedule.getPreferredTime());
        schedule.updateNextCheckInDue(nextDue);

        checkInScheduleRepository.save(schedule);
        log.info("체크인 스케줄 일시 중지: userId={}, reason={}, until={}", userId, request.reason(), endDate);

        return CheckInScheduleResponse.from(schedule);
    }

    /**
     * 일시 중지된 체크인 스케줄을 재개합니다.
     */
    @Transactional
    public CheckInScheduleResponse resumeSchedule(Long userId) {
        CheckInSchedule schedule = checkInScheduleRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("체크인 스케줄이 설정되지 않았습니다"));

        if (!schedule.isPaused()) {
            throw BusinessException.badRequest("일시 중지 상태가 아닙니다");
        }

        schedule.resume();

        LocalDateTime nextDue = calculateNextCheckInDue(schedule);
        schedule.updateNextCheckInDue(nextDue);

        checkInScheduleRepository.save(schedule);
        log.info("체크인 스케줄 재활성화: userId={}", userId);

        return CheckInScheduleResponse.from(schedule);
    }

    private void validateRequest(CheckInScheduleRequest request) {
        if (request.intervalHours() != null) {
            if (request.intervalHours() < 12 || request.intervalHours() > 48) {
                throw BusinessException.badRequest("체크인 간격은 12~48시간 사이여야 합니다");
            }
        }
        if (request.pauseUntil() != null) {
            long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), request.pauseUntil());
            if (daysBetween > 30) {
                throw BusinessException.badRequest("임시 비활성 기간은 최대 30일입니다");
            }
            if (daysBetween < 0) {
                throw BusinessException.badRequest("비활성 종료일은 오늘 이후여야 합니다");
            }
        }
    }

    /**
     * 다음 체크인 예정 시간을 계산합니다.
     */
    public LocalDateTime calculateNextCheckInDue(CheckInSchedule schedule) {
        if (schedule.isPaused()) {
            // 일시 중지 종료 후 선호 시간에 체크인
            LocalDate resumeDate = schedule.getPauseEndDate() != null
                    ? schedule.getPauseEndDate()
                    : schedule.getPauseUntil();
            if (resumeDate != null) {
                return resumeDate.atTime(schedule.getPreferredTime());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextDue = now.toLocalDate().atTime(schedule.getPreferredTime());

        // 이미 오늘의 선호 시간이 지났으면 다음 간격으로
        if (nextDue.isBefore(now)) {
            nextDue = nextDue.plusHours(schedule.getIntervalHours());
        }

        return nextDue;
    }
}
