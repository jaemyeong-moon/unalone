package com.project.api.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "parent_id")
    private Long parentId;

    @Builder
    public Comment(CommunityPost post, User user, String content, Long parentId) {
        this.post = post;
        this.user = user;
        this.content = content;
        this.parentId = parentId;
    }

    public void updateContent(String content) {
        if (content != null) this.content = content;
    }
}
