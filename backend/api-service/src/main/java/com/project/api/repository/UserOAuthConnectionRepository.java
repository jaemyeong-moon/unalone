package com.project.api.repository;

import com.project.api.domain.User;
import com.project.api.domain.UserOAuthConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserOAuthConnectionRepository extends JpaRepository<UserOAuthConnection, Long> {

    Optional<UserOAuthConnection> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);

    List<UserOAuthConnection> findByUser(User user);

    Optional<UserOAuthConnection> findByUserAndOauthProvider(User user, String oauthProvider);

    boolean existsByOauthProviderAndOauthId(String oauthProvider, String oauthId);

    long countByUser(User user);

    void deleteByUserAndOauthProvider(User user, String oauthProvider);
}
