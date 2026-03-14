package com.project.admin.repository;

import com.project.admin.domain.PostQualityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostQualityLogRepository extends JpaRepository<PostQualityLog, Long> {

    List<PostQualityLog> findByPostIdOrderByCreatedAtDesc(Long postId);
}
