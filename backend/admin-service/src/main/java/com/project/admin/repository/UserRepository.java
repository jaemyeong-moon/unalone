package com.project.admin.repository;

import com.project.admin.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByStatus(User.UserStatus status, Pageable pageable);
    long countByStatus(User.UserStatus status);
    @Query("SELECT u FROM User u WHERE u.role = 'ROLE_USER' AND u.status = 'ACTIVE'")
    List<User> findActiveUsers();
}
