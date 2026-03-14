package com.project.api.service;

import com.project.api.domain.CareMatch;
import com.project.api.domain.CareVisit;
import com.project.api.domain.Volunteer;
import com.project.api.domain.enums.CareVisitStatus;
import com.project.api.dto.care.CareVisitReportRequest;
import com.project.api.dto.care.CareVisitRequest;
import com.project.api.dto.care.CareVisitResponse;
import com.project.api.exception.BusinessException;
import com.project.api.repository.CareMatchRepository;
import com.project.api.repository.CareVisitRepository;
import com.project.api.repository.VolunteerRepository;
import com.project.common.config.KafkaConfig;
import com.project.common.event.CareVisitCompletedEvent;
import com.project.common.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CareVisitService {

    private static final int TRUST_SCORE_COMPLETED = 2;
    private static final int TRUST_SCORE_NO_SHOW = -5;

    private final CareVisitRepository careVisitRepository;
    private final CareMatchRepository careMatchRepository;
    private final VolunteerRepository volunteerRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public CareVisitResponse scheduleVisit(Long userId, CareVisitRequest request) {
        CareMatch careMatch = careMatchRepository.findById(request.careMatchId())
                .orElseThrow(() -> BusinessException.notFound("매칭 정보를 찾을 수 없습니다"));

        Volunteer volunteer = volunteerRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("자원봉사자 정보를 찾을 수 없습니다"));

        if (!careMatch.getVolunteerId().equals(volunteer.getId())) {
            throw BusinessException.unauthorized("해당 매칭의 자원봉사자만 방문을 예약할 수 있습니다");
        }

        CareVisit visit = CareVisit.builder()
                .careMatchId(careMatch.getId())
                .volunteerId(volunteer.getId())
                .receiverId(careMatch.getReceiverId())
                .scheduledDate(request.scheduledDate())
                .scheduledTime(request.scheduledTime())
                .build();

        careVisitRepository.save(visit);
        return CareVisitResponse.from(visit);
    }

    @Transactional(readOnly = true)
    public List<CareVisitResponse> getVisits(Long userId, Long matchId,
                                              LocalDate startDate, LocalDate endDate) {
        if (matchId != null) {
            return careVisitRepository.findByCareMatchId(matchId).stream()
                    .map(CareVisitResponse::from)
                    .toList();
        }

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now().plusMonths(1);

        // 자원봉사자인 경우 자원봉사자 ID로 조회, 아니면 수신자 ID로 조회
        Volunteer volunteer = volunteerRepository.findByUserId(userId).orElse(null);
        if (volunteer != null) {
            return careVisitRepository.findByVolunteerIdAndScheduledDateBetween(
                    volunteer.getId(), start, end).stream()
                    .map(CareVisitResponse::from)
                    .toList();
        }

        return careVisitRepository.findByReceiverIdAndScheduledDateBetween(userId, start, end).stream()
                .map(CareVisitResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CareVisitResponse getVisitDetail(Long userId, Long visitId) {
        CareVisit visit = careVisitRepository.findById(visitId)
                .orElseThrow(() -> BusinessException.notFound("방문 정보를 찾을 수 없습니다"));

        Volunteer volunteer = volunteerRepository.findByUserId(userId).orElse(null);
        Long volunteerId = volunteer != null ? volunteer.getId() : null;

        if (!visit.getReceiverId().equals(userId) &&
                (volunteerId == null || !visit.getVolunteerId().equals(volunteerId))) {
            throw BusinessException.unauthorized("해당 방문에 대한 조회 권한이 없습니다");
        }

        return CareVisitResponse.from(visit);
    }

    @Transactional
    public CareVisitResponse submitReport(Long userId, Long visitId, CareVisitReportRequest request) {
        CareVisit visit = careVisitRepository.findById(visitId)
                .orElseThrow(() -> BusinessException.notFound("방문 정보를 찾을 수 없습니다"));

        Volunteer volunteer = volunteerRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("자원봉사자 정보를 찾을 수 없습니다"));

        if (!visit.getVolunteerId().equals(volunteer.getId())) {
            throw BusinessException.unauthorized("해당 방문의 자원봉사자만 보고서를 제출할 수 있습니다");
        }

        if (visit.getStatus() != CareVisitStatus.SCHEDULED) {
            throw BusinessException.badRequest("예정된 방문만 보고서를 제출할 수 있습니다");
        }

        visit.submitReport(request.reportContent(), request.receiverCondition(), request.specialNotes());

        // 신뢰 점수 업데이트: 완료 시 +2
        volunteer.addTrustScore(TRUST_SCORE_COMPLETED);
        volunteer.incrementTotalVisits();

        // Kafka 이벤트 발행
        eventPublisher.publish(KafkaConfig.TOPIC_CARE_VISIT_EVENTS,
                new CareVisitCompletedEvent(
                        visit.getId(),
                        visit.getCareMatchId(),
                        volunteer.getId(),
                        visit.getReceiverId(),
                        request.receiverCondition().name(),
                        request.specialNotes(),
                        visit.getVisitedAt()
                ));

        return CareVisitResponse.from(visit);
    }

    @Transactional
    public CareVisitResponse cancelVisit(Long userId, Long visitId) {
        CareVisit visit = careVisitRepository.findById(visitId)
                .orElseThrow(() -> BusinessException.notFound("방문 정보를 찾을 수 없습니다"));

        Volunteer volunteer = volunteerRepository.findByUserId(userId).orElse(null);
        Long volunteerId = volunteer != null ? volunteer.getId() : null;

        if (!visit.getReceiverId().equals(userId) &&
                (volunteerId == null || !visit.getVolunteerId().equals(volunteerId))) {
            throw BusinessException.unauthorized("해당 방문에 대한 취소 권한이 없습니다");
        }

        if (visit.getStatus() != CareVisitStatus.SCHEDULED) {
            throw BusinessException.badRequest("예정된 방문만 취소할 수 있습니다");
        }

        visit.cancel();
        return CareVisitResponse.from(visit);
    }
}
