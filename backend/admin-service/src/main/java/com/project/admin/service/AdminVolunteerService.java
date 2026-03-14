package com.project.admin.service;

import com.project.admin.domain.Volunteer;
import com.project.admin.dto.VolunteerAdminResponse;
import com.project.admin.exception.ResourceNotFoundException;
import com.project.admin.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminVolunteerService {

    private final VolunteerRepository volunteerRepository;

    public Page<VolunteerAdminResponse> getVolunteers(String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            return volunteerRepository.findByStatus(status, pageable)
                    .map(VolunteerAdminResponse::from);
        }
        return volunteerRepository.findAll(pageable)
                .map(VolunteerAdminResponse::from);
    }

    @Transactional
    public void approveVolunteer(Long volunteerId) {
        Volunteer volunteer = volunteerRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("자원봉사자를 찾을 수 없습니다: " + volunteerId));
        volunteer.approve();
    }
}
