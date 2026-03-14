package com.project.admin.repository;

import com.project.admin.domain.Volunteer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {

    Page<Volunteer> findByStatus(String status, Pageable pageable);

    long countByStatus(String status);
}
