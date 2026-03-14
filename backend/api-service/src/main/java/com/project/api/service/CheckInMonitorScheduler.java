package com.project.api.service;

import com.project.api.domain.CheckInSchedule;
import com.project.api.repository.CheckInScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 체크인 모니터링 스케줄러.
 * 30분마다 모든 활성 스케줄을 점검하여 미응답 체크인에 대한 에스컬레이션을 트리거합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInMonitorScheduler {

    private final CheckInScheduleRepository checkInScheduleRepository;
    private final EscalationService escalationService;

    /**
     * 30분마다 실행: 체크인 예정 시간이 지난 스케줄을 조회하여 에스컬레이션을 처리합니다.
     */
    @Scheduled(fixedRate = 1800000) // 30분
    public void monitorCheckIns() {
        log.info("체크인 모니터링 스케줄러 실행");

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        List<CheckInSchedule> overdueSchedules = checkInScheduleRepository.findOverdueSchedules(now, today);

        log.info("미응답 체크인 대상: {}건", overdueSchedules.size());

        for (CheckInSchedule schedule : overdueSchedules) {
            try {
                // 오늘이 활성 요일인지 확인
                String dayOfWeek = today.getDayOfWeek().name().substring(0, 3);
                if (!schedule.isActiveOnDay(dayOfWeek)) {
                    continue;
                }

                escalationService.processEscalation(schedule);
            } catch (Exception e) {
                log.error("에스컬레이션 처리 실패: scheduleId={}, userId={}, error={}",
                        schedule.getId(), schedule.getUser().getId(), e.getMessage());
            }
        }
    }
}
