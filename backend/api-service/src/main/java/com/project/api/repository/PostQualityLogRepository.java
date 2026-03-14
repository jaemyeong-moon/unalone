package com.project.api.repository;

import com.project.api.domain.PostQualityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostQualityLogRepository extends JpaRepository<PostQualityLog, Long> {

    List<PostQualityLog> findByPostIdOrderByCreatedAtDesc(Long postId);
}
