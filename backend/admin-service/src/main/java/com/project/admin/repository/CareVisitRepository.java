package com.project.admin.repository;

import com.project.admin.domain.CareVisit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareVisitRepository extends JpaRepository<CareVisit, Long> {

    Page<CareVisit> findByStatusOrderByScheduledDateDesc(String status, Pageable pageable);

    Page<CareVisit> findByReceiverConditionOrderByScheduledDateDesc(String condition, Pageable pageable);

    Page<CareVisit> findAllByOrderByScheduledDateDesc(Pageable pageable);
}
