package com.project.api.repository;

import com.project.api.domain.CheckInSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CheckInScheduleRepository extends JpaRepository<CheckInSchedule, Long> {

    Optional<CheckInSchedule> findByUserId(Long userId);

    @Query("SELECT cs FROM CheckInSchedule cs JOIN FETCH cs.user " +
           "WHERE cs.nextCheckInDue <= :now " +
           "AND (cs.pauseUntil IS NULL OR cs.pauseUntil < :today) " +
           "AND cs.user.status = 'ACTIVE'")
    List<CheckInSchedule> findOverdueSchedules(@Param("now") LocalDateTime now,
                                                @Param("today") LocalDate today);

    @Query("SELECT cs FROM CheckInSchedule cs JOIN FETCH cs.user " +
           "WHERE cs.user.status = 'ACTIVE' " +
           "AND (cs.pauseUntil IS NULL OR cs.pauseUntil < :today)")
    List<CheckInSchedule> findActiveSchedules(@Param("today") LocalDate today);
}
