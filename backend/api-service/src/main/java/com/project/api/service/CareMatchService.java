package com.project.api.service;

import com.project.api.domain.CareMatch;
import com.project.api.domain.Volunteer;
import com.project.api.domain.enums.CareMatchStatus;
import com.project.api.domain.enums.NotificationType;
import com.project.api.dto.care.CareMatchResponse;
import com.project.api.exception.BusinessException;
import com.project.api.repository.CareMatchRepository;
import com.project.api.repository.VolunteerRepository;
import com.project.common.config.KafkaConfig;
import com.project.common.event.CareMatchCreatedEvent;
import com.project.common.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CareMatchService {

    private static final int MAX_ACTIVE_MATCHES = 5;

    private final CareMatchRepository careMatchRepository;
    private final VolunteerRepository volunteerRepository;
    private final EventPublisher eventPublisher;
    private final NotificationService notificationService;

    @Transactional
    public CareMatchResponse createMatch(Long userId, Long receiverId) {
        Volunteer volunteer = volunteerRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("자원봉사자 정보를 찾을 수 없습니다"));

        if (careMatchRepository.countByVolunteerIdAndStatus(volunteer.getId(), CareMatchStatus.ACTIVE) >= MAX_ACTIVE_MATCHES) {
            throw BusinessException.badRequest("활성 매칭은 최대 %d건까지 가능합니다".formatted(MAX_ACTIVE_MATCHES));
        }

        CareMatch careMatch = CareMatch.builder()
                .volunteerId(volunteer.getId())
                .receiverId(receiverId)
                .distance(null)
                .build();

        careMatchRepository.save(careMatch);

        eventPublisher.publish(KafkaConfig.TOPIC_CARE_MATCH_EVENTS,
                new CareMatchCreatedEvent(careMatch.getId(), volunteer.getId(),
                        receiverId, careMatch.getDistance()));

        // 자원봉사자에게 매칭 알림
        notificationService.createNotification(
                userId,
                NotificationType.CARE_MATCH,
                "돌봄 매칭 생성",
                "새로운 돌봄 매칭이 생성되었습니다.",
                careMatch.getId(),
                "CARE_MATCH"
        );

        // 돌봄 대상자에게 매칭 알림
        notificationService.createNotification(
                receiverId,
                NotificationType.CARE_MATCH,
                "돌봄 매칭 알림",
                "새로운 돌봄 자원봉사자가 매칭되었습니다.",
                careMatch.getId(),
                "CARE_MATCH"
        );

        return CareMatchResponse.from(careMatch);
    }

    @Transactional(readOnly = true)
    public List<CareMatchResponse> getMyMatches(Long userId) {
        return careMatchRepository.findByVolunteerIdOrReceiverId(userId).stream()
                .map(CareMatchResponse::from)
                .toList();
    }

    @Transactional
    public CareMatchResponse acceptMatch(Long userId, Long matchId) {
        CareMatch careMatch = findMatchById(matchId);
        validateMatchParticipant(careMatch, userId);

        careMatch.accept();
        return CareMatchResponse.from(careMatch);
    }

    @Transactional
    public CareMatchResponse completeMatch(Long userId, Long matchId) {
        CareMatch careMatch = findMatchById(matchId);
        validateMatchParticipant(careMatch, userId);

        careMatch.complete();
        return CareMatchResponse.from(careMatch);
    }

    @Transactional
    public CareMatchResponse cancelMatch(Long userId, Long matchId) {
        CareMatch careMatch = findMatchById(matchId);
        validateMatchParticipant(careMatch, userId);

        careMatch.cancel();
        return CareMatchResponse.from(careMatch);
    }

    private CareMatch findMatchById(Long matchId) {
        return careMatchRepository.findById(matchId)
                .orElseThrow(() -> BusinessException.notFound("매칭 정보를 찾을 수 없습니다"));
    }

    private void validateMatchParticipant(CareMatch careMatch, Long userId) {
        Volunteer volunteer = volunteerRepository.findByUserId(userId).orElse(null);
        Long volunteerId = volunteer != null ? volunteer.getId() : null;

        if (!careMatch.getReceiverId().equals(userId) &&
                (volunteerId == null || !careMatch.getVolunteerId().equals(volunteerId))) {
            throw BusinessException.unauthorized("해당 매칭에 대한 권한이 없습니다");
        }
    }
}
