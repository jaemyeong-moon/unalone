package com.project.admin.repository;

import com.project.admin.domain.CareMatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareMatchRepository extends JpaRepository<CareMatch, Long> {

    long countByStatus(String status);
}
