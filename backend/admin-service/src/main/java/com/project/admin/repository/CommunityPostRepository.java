package com.project.admin.repository;

import com.project.admin.domain.CommunityPost;
import com.project.admin.domain.QualityGrade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    @Query("SELECT p FROM CommunityPost p JOIN FETCH p.user WHERE p.qualityScore IS NOT NULL AND p.qualityScore < :threshold ORDER BY p.qualityScore ASC")
    Page<CommunityPost> findByQualityScoreLessThanWithUser(@Param("threshold") int threshold, Pageable pageable);

    @Query("SELECT AVG(p.qualityScore) FROM CommunityPost p WHERE p.qualityScore IS NOT NULL")
    Double averageQualityScore();

    @Query("SELECT p.qualityGrade, COUNT(p) FROM CommunityPost p WHERE p.qualityGrade IS NOT NULL GROUP BY p.qualityGrade")
    List<Object[]> countByQualityGradeGrouped();

    @Query("SELECT COUNT(p) FROM CommunityPost p WHERE p.qualityScore IS NOT NULL")
    long countScoredPosts();
}
