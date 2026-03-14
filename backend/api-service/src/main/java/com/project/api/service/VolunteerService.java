package com.project.api.service;

import com.project.api.domain.Volunteer;
import com.project.api.dto.care.VolunteerRequest;
import com.project.api.dto.care.VolunteerResponse;
import com.project.api.exception.BusinessException;
import com.project.api.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VolunteerService {

    private final VolunteerRepository volunteerRepository;

    @Transactional
    public VolunteerResponse register(Long userId, VolunteerRequest request) {
        if (volunteerRepository.existsByUserId(userId)) {
            throw BusinessException.conflict("이미 자원봉사자로 등록되어 있습니다");
        }

        Volunteer volunteer = Volunteer.builder()
                .userId(userId)
                .availableDays(request.availableDays())
                .availableTimeStart(request.availableTimeStart())
                .availableTimeEnd(request.availableTimeEnd())
                .radius(request.radius())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .introduction(request.introduction())
                .build();

        volunteerRepository.save(volunteer);
        return VolunteerResponse.from(volunteer);
    }

    @Transactional(readOnly = true)
    public VolunteerResponse getMyStatus(Long userId) {
        Volunteer volunteer = volunteerRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("자원봉사자 정보를 찾을 수 없습니다"));
        return VolunteerResponse.from(volunteer);
    }

    @Transactional
    public VolunteerResponse update(Long userId, VolunteerRequest request) {
        Volunteer volunteer = volunteerRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("자원봉사자 정보를 찾을 수 없습니다"));

        volunteer.update(
                request.availableDays(),
                request.availableTimeStart(),
                request.availableTimeEnd(),
                request.radius(),
                request.latitude(),
                request.longitude(),
                request.introduction()
        );

        return VolunteerResponse.from(volunteer);
    }

    @Transactional
    public void withdraw(Long userId) {
        Volunteer volunteer = volunteerRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("자원봉사자 정보를 찾을 수 없습니다"));
        volunteer.withdraw();
    }

    @Transactional(readOnly = true)
    public List<VolunteerResponse> findNearby(Double latitude, Double longitude, Double radius) {
        double searchRadius = radius != null ? radius : 2.0;
        return volunteerRepository.findNearbyApproved(latitude, longitude, searchRadius).stream()
                .map(VolunteerResponse::from)
                .toList();
    }
}
