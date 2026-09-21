package com.dsalearner.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_hints", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","hint_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserHint {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hint_id", nullable = false)
    private Hint hint;

    @Column(nullable = false)
    @Builder.Default
    private Instant unlockedAt = Instant.now();
}
