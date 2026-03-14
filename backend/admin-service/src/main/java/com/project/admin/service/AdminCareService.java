package com.project.admin.service;

import com.project.admin.dto.CareVisitAdminResponse;
import com.project.admin.repository.CareVisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminCareService {

    private final CareVisitRepository careVisitRepository;

    public Page<CareVisitAdminResponse> getVisits(Pageable pageable) {
        return careVisitRepository.findAllByOrderByScheduledDateDesc(pageable)
                .map(CareVisitAdminResponse::from);
    }

    public Page<CareVisitAdminResponse> getReportsByCondition(String condition, Pageable pageable) {
        if (condition != null && !condition.isBlank()) {
            return careVisitRepository
                    .findByReceiverConditionOrderByScheduledDateDesc(condition, pageable)
                    .map(CareVisitAdminResponse::from);
        }
        return careVisitRepository.findAllByOrderByScheduledDateDesc(pageable)
                .map(CareVisitAdminResponse::from);
    }
}
