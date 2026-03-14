package com.project.api.repository;

import com.project.api.domain.CareMatch;
import com.project.api.domain.enums.CareMatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CareMatchRepository extends JpaRepository<CareMatch, Long> {

    @Query("SELECT m FROM CareMatch m WHERE m.volunteerId = :userId OR m.receiverId = :userId")
    List<CareMatch> findByVolunteerIdOrReceiverId(@Param("userId") Long userId);

    Optional<CareMatch> findByReceiverIdAndStatus(Long receiverId, CareMatchStatus status);

    List<CareMatch> findByVolunteerIdAndStatus(Long volunteerId, CareMatchStatus status);

    long countByVolunteerIdAndStatus(Long volunteerId, CareMatchStatus status);
}
