package com.project.api.repository;

import com.project.api.domain.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuardianRepository extends JpaRepository<Guardian, Long> {

    List<Guardian> findByUserId(Long userId);

    long countByUserId(Long userId);
}
