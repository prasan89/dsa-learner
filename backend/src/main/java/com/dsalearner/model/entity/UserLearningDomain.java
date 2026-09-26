package com.dsalearner.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_learning_domains")
@IdClass(UserLearningDomainId.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserLearningDomain {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Id
    @Column(name = "domain_code", nullable = false)
    private String domainCode;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private Instant enrolledAt;

    @PrePersist
    void prePersist() {
        if (enrolledAt == null) enrolledAt = Instant.now();
    }
}
