package com.project.api.repository;

import com.project.api.domain.Volunteer;
import com.project.api.domain.enums.VolunteerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {

    Optional<Volunteer> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    Page<Volunteer> findByStatus(VolunteerStatus status, Pageable pageable);

    @Query("SELECT v FROM Volunteer v WHERE v.status = 'APPROVED' " +
            "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(v.latitude)) " +
            "* cos(radians(v.longitude) - radians(:longitude)) " +
            "+ sin(radians(:latitude)) * sin(radians(v.latitude)))) <= :radius")
    List<Volunteer> findNearbyApproved(@Param("latitude") Double latitude,
                                       @Param("longitude") Double longitude,
                                       @Param("radius") Double radius);
}
