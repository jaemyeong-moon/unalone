package com.project.api.repository;

import com.project.api.domain.CommunityPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    @Query("SELECT p FROM CommunityPost p JOIN FETCH p.user ORDER BY p.createdAt DESC")
    Page<CommunityPost> findAllWithUser(Pageable pageable);

    @Query("SELECT p FROM CommunityPost p JOIN FETCH p.user WHERE p.category = :category ORDER BY p.createdAt DESC")
    Page<CommunityPost> findByCategoryWithUser(@Param("category") CommunityPost.PostCategory category, Pageable pageable);

    Page<CommunityPost> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
