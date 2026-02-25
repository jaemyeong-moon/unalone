package com.project.admin.repository;

import com.project.admin.domain.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    @Query("SELECT c FROM CheckIn c WHERE c.user.id = :userId ORDER BY c.checkedAt DESC LIMIT 1")
    Optional<CheckIn> findLatestByUserId(@Param("userId") Long userId);
    long countByCheckedAtAfter(LocalDateTime after);
}
