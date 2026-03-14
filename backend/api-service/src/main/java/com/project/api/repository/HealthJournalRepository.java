package com.project.api.repository;

import com.project.api.domain.HealthJournal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HealthJournalRepository extends JpaRepository<HealthJournal, Long> {

    Optional<HealthJournal> findByUserIdAndDate(Long userId, LocalDate date);

    Page<HealthJournal> findByUserIdOrderByDateDesc(Long userId, Pageable pageable);

    @Query("SELECT hj FROM HealthJournal hj WHERE hj.user.id = :userId " +
           "AND hj.date BETWEEN :startDate AND :endDate ORDER BY hj.date ASC")
    List<HealthJournal> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT AVG(hj.moodScore) FROM HealthJournal hj WHERE hj.user.id = :userId " +
           "AND hj.date BETWEEN :startDate AND :endDate AND hj.moodScore IS NOT NULL")
    Double findAverageMoodScore(@Param("userId") Long userId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);

    @Query("SELECT AVG(hj.healthScore) FROM HealthJournal hj WHERE hj.user.id = :userId " +
           "AND hj.date BETWEEN :startDate AND :endDate AND hj.healthScore IS NOT NULL")
    Double findAverageHealthScore(@Param("userId") Long userId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    long countByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT hj FROM HealthJournal hj WHERE hj.user.id = :userId " +
           "AND hj.moodScore IS NOT NULL ORDER BY hj.date DESC")
    List<HealthJournal> findRecentWithMoodScore(@Param("userId") Long userId, Pageable pageable);

    void deleteAllByUserId(Long userId);
}
