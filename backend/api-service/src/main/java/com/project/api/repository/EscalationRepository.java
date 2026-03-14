package com.project.api.repository;

import com.project.api.domain.Escalation;
import com.project.api.domain.EscalationStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EscalationRepository extends JpaRepository<Escalation, Long> {

    Optional<Escalation> findByUserIdAndResolvedFalse(Long userId);

    List<Escalation> findByResolvedFalse();

    Page<Escalation> findByUserIdOrderByTriggeredAtDesc(Long userId, Pageable pageable);

    @Query("SELECT e FROM Escalation e WHERE e.resolved = false AND e.stage = :stage")
    List<Escalation> findActiveByStage(@Param("stage") EscalationStage stage);

    long countByUserIdAndResolvedFalse(Long userId);
}
