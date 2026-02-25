package com.project.api.repository;

import com.project.api.domain.CheckIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    Page<CheckIn> findByUserIdOrderByCheckedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT c FROM CheckIn c WHERE c.user.id = :userId ORDER BY c.checkedAt DESC LIMIT 1")
    Optional<CheckIn> findLatestByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM CheckIn c WHERE c.user.id = :userId AND c.checkedAt BETWEEN :start AND :end ORDER BY c.checkedAt DESC")
    List<CheckIn> findByUserIdAndDateRange(@Param("userId") Long userId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    long countByUserIdAndCheckedAtAfter(Long userId, LocalDateTime after);
}
