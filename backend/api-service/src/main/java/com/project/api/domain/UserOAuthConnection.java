package com.project.api.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_oauth_connections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"oauth_provider", "oauth_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOAuthConnection extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "oauth_provider", nullable = false, length = 20)
    private String oauthProvider;

    @Column(name = "oauth_id", nullable = false, length = 100)
    private String oauthId;

    @Column(name = "oauth_email")
    private String oauthEmail;

    @Column(name = "oauth_nickname", length = 100)
    private String oauthNickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "connected_at", nullable = false)
    private LocalDateTime connectedAt;

    @Builder
    public UserOAuthConnection(User user, String oauthProvider, String oauthId,
                               String oauthEmail, String oauthNickname, String profileImageUrl) {
        this.user = user;
        this.oauthProvider = oauthProvider;
        this.oauthId = oauthId;
        this.oauthEmail = oauthEmail;
        this.oauthNickname = oauthNickname;
        this.profileImageUrl = profileImageUrl;
        this.connectedAt = LocalDateTime.now();
    }
}
