package com.project.event.repository;

import com.project.event.domain.Alert;
import com.project.event.domain.AlertStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AlertRepository extends MongoRepository<Alert, String> {

    List<Alert> findByUserIdAndStatus(Long userId, AlertStatus status);

    long countByUserIdAndStatus(Long userId, AlertStatus status);
}
