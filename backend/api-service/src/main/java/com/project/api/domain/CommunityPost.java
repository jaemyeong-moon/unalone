package com.project.api.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "community_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPost extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostCategory category;

    public enum PostCategory {
        DAILY, HEALTH, HOBBY, HELP, NOTICE
    }

    @Builder
    public CommunityPost(User user, String title, String content, PostCategory category) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.category = category != null ? category : PostCategory.DAILY;
    }

    public void update(String title, String content, PostCategory category) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (category != null) this.category = category;
    }
}
