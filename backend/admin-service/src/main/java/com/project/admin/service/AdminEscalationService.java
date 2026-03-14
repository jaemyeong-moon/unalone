package com.project.admin.service;

import com.project.admin.domain.Escalation;
import com.project.admin.dto.EscalationAdminResponse;
import com.project.admin.exception.ResourceNotFoundException;
import com.project.admin.repository.EscalationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminEscalationService {

    private final EscalationRepository escalationRepository;

    public Page<EscalationAdminResponse> getActiveEscalations(String stage, Pageable pageable) {
        if (stage != null && !stage.isBlank()) {
            return escalationRepository
                    .findByResolvedFalseAndStageOrderByTriggeredAtDesc(stage, pageable)
                    .map(EscalationAdminResponse::from);
        }
        return escalationRepository
                .findByResolvedFalseOrderByTriggeredAtDesc(pageable)
                .map(EscalationAdminResponse::from);
    }

    @Transactional
    public void resolveEscalation(Long escalationId) {
        Escalation escalation = escalationRepository.findById(escalationId)
                .orElseThrow(() -> new ResourceNotFoundException("에스컬레이션을 찾을 수 없습니다: " + escalationId));
        escalation.resolve();
    }
}
