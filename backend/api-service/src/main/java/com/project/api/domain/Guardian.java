package com.project.api.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "guardians")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Guardian extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 30)
    private String relationship;

    @Builder
    public Guardian(User user, String name, String phone, String relationship) {
        this.user = user;
        this.name = name;
        this.phone = phone;
        this.relationship = relationship;
    }

    public void update(String name, String phone, String relationship) {
        if (name != null) this.name = name;
        if (phone != null) this.phone = phone;
        if (relationship != null) this.relationship = relationship;
    }
}
