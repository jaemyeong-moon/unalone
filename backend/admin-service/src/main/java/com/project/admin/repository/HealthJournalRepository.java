package com.project.admin.repository;

import com.project.admin.domain.HealthJournal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface HealthJournalRepository extends JpaRepository<HealthJournal, Long> {

    @Query("SELECT COALESCE(AVG(h.healthScore), 0) FROM HealthJournal h WHERE h.date >= :since AND h.healthScore IS NOT NULL")
    double avgHealthScoreSince(@Param("since") LocalDate since);

    @Query("SELECT COUNT(h) FROM HealthJournal h WHERE h.date >= :since AND h.healthScore IS NOT NULL AND h.healthScore <= 30")
    long countCriticalHealthAlertsSince(@Param("since") LocalDate since);
}
