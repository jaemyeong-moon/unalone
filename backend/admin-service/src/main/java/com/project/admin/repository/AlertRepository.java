package com.project.admin.repository;

import com.project.admin.domain.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AlertRepository extends MongoRepository<Alert, String> {
    Page<Alert> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    List<Alert> findByUserIdAndStatus(Long userId, String status);
    long countByStatus(String status);
    long countByLevel(String level);
}
