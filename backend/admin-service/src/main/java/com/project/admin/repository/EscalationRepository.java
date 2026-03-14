package com.project.admin.repository;

import com.project.admin.domain.Escalation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EscalationRepository extends JpaRepository<Escalation, Long> {

    long countByResolvedFalse();

    Page<Escalation> findByResolvedFalseOrderByTriggeredAtDesc(Pageable pageable);

    Page<Escalation> findByResolvedFalseAndStageOrderByTriggeredAtDesc(String stage, Pageable pageable);
}
