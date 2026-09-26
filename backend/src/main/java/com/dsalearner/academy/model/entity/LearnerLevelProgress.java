package com.dsalearner.academy.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "learner_level_progress")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LearnerLevelProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID curriculumId;

    @Column(nullable = false, length = 4)
    private String cefrLevel;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "NOT_STARTED";

    @Column(nullable = false)
    @Builder.Default
    private int lessonsTotal = 0;

    @Column(nullable = false)
    @Builder.Default
    private int lessonsCompleted = 0;

    @Column(precision = 5, scale = 2)
    private BigDecimal avgScore;

    private Instant unlockedAt;
    private Instant completedAt;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    private Instant updatedAt;
}
