package com.project.api.repository;

import com.project.api.domain.CareVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CareVisitRepository extends JpaRepository<CareVisit, Long> {

    List<CareVisit> findByCareMatchId(Long careMatchId);

    List<CareVisit> findByVolunteerIdAndScheduledDateBetween(Long volunteerId,
                                                              LocalDate startDate,
                                                              LocalDate endDate);

    List<CareVisit> findByReceiverIdAndScheduledDateBetween(Long receiverId,
                                                             LocalDate startDate,
                                                             LocalDate endDate);
}
