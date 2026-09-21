package com.dsalearner.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pattern_mastery", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","pattern_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PatternMastery {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pattern_id", nullable = false)
    private Pattern pattern;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MasteryStatus status = MasteryStatus.NOT_STARTED;

    @Column(nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Column(name = "mastery_score", nullable = false)
    @Builder.Default
    private java.math.BigDecimal masteryScore = java.math.BigDecimal.ZERO;

    @Column(name = "problems_solved", nullable = false)
    @Builder.Default
    private int problemsSolved = 0;

    @Column(name = "problems_attempted", nullable = false)
    @Builder.Default
    private int problemsAttempted = 0;

    public enum MasteryStatus { NOT_STARTED, LEARNING, PRACTICED, MASTERED }
}
