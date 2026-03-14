package com.project.api.repository;

import com.project.api.domain.CommunityPost;
import com.project.api.domain.QualityGrade;
import com.project.api.domain.TranslationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    @Query("SELECT p FROM CommunityPost p JOIN FETCH p.user ORDER BY p.createdAt DESC")
    Page<CommunityPost> findAllWithUser(Pageable pageable);

    @Query("SELECT p FROM CommunityPost p JOIN FETCH p.user WHERE p.category = :category ORDER BY p.createdAt DESC")
    Page<CommunityPost> findByCategoryWithUser(@Param("category") CommunityPost.PostCategory category, Pageable pageable);

    Page<CommunityPost> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // === Translation queries ===

    List<CommunityPost> findByTranslationStatus(TranslationStatus status, Pageable pageable);

    List<CommunityPost> findByTranslationStatusAndTranslationRetryCountLessThan(
            TranslationStatus status, int maxRetries, Pageable pageable);

    // === Quality scoring queries ===

    List<CommunityPost> findByQualityScoreIsNull(Pageable pageable);

    @Query("SELECT p FROM CommunityPost p JOIN FETCH p.user WHERE p.qualityGrade = :grade ORDER BY p.qualityScore DESC")
    Page<CommunityPost> findByQualityGradeWithUser(@Param("grade") QualityGrade grade, Pageable pageable);

    @Query("SELECT p FROM CommunityPost p JOIN FETCH p.user WHERE p.qualityScore IS NOT NULL ORDER BY p.qualityScore DESC")
    Page<CommunityPost> findAllByQualityScoreDesc(Pageable pageable);

    @Query("SELECT p FROM CommunityPost p JOIN FETCH p.user WHERE p.qualityScore IS NOT NULL AND p.qualityScore < :threshold ORDER BY p.qualityScore ASC")
    Page<CommunityPost> findByQualityScoreLessThanWithUser(@Param("threshold") int threshold, Pageable pageable);

    @Query("SELECT AVG(p.qualityScore) FROM CommunityPost p WHERE p.user.id = :userId AND p.qualityScore IS NOT NULL")
    Double averageQualityScoreByUserId(@Param("userId") Long userId);

    long countByUserId(Long userId);

    // === Quality stats queries ===

    @Query("SELECT AVG(p.qualityScore) FROM CommunityPost p WHERE p.qualityScore IS NOT NULL")
    Double averageQualityScore();

    @Query("SELECT p.qualityGrade, COUNT(p) FROM CommunityPost p WHERE p.qualityGrade IS NOT NULL GROUP BY p.qualityGrade")
    List<Object[]> countByQualityGradeGrouped();

    @Query("SELECT COUNT(p) FROM CommunityPost p WHERE p.qualityScore IS NOT NULL")
    long countScoredPosts();
}
