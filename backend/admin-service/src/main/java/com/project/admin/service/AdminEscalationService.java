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

import java.util.LinkedHashMap;
import java.util.Map;

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

    /**
     * 에스컬레이션 요약 정보를 반환합니다 (레벨별 카운트).
     */
    public Map<String, Long> getEscalationSummary() {
        Map<String, Long> summary = new LinkedHashMap<>();
        summary.put("REMINDER", escalationRepository.countByStageAndResolvedFalse("REMINDER"));
        summary.put("WARNING", escalationRepository.countByStageAndResolvedFalse("WARNING"));
        summary.put("DANGER", escalationRepository.countByStageAndResolvedFalse("DANGER"));
        summary.put("CRITICAL", escalationRepository.countByStageAndResolvedFalse("CRITICAL"));
        summary.put("totalActive", escalationRepository.countByResolvedFalse());
        return summary;
    }

    @Transactional
    public void resolveEscalation(Long escalationId, String notes) {
        Escalation escalation = escalationRepository.findById(escalationId)
                .orElseThrow(() -> new ResourceNotFoundException("에스컬레이션을 찾을 수 없습니다: " + escalationId));
        if (notes != null) {
            escalation.resolveByAdmin("ADMIN", notes);
        } else {
            escalation.resolve();
        }
    }

    @Transactional
    public void resolveEscalation(Long escalationId) {
        resolveEscalation(escalationId, null);
    }

    public Page<EscalationAdminResponse> getUserEscalations(Long userId, Pageable pageable) {
        return escalationRepository.findByUserIdOrderByTriggeredAtDesc(userId, pageable)
                .map(EscalationAdminResponse::from);
    }
}
