package com.project.api.repository;

import com.project.api.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId AND c.parentId IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(@Param("postId") Long postId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.parentId = :parentId ORDER BY c.createdAt ASC")
    List<Comment> findByParentIdOrderByCreatedAtAsc(@Param("parentId") Long parentId);

    long countByPostId(Long postId);
}
