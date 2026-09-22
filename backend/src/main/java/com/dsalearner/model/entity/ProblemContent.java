package com.dsalearner.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "problem_content")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProblemContent {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false, unique = true)
    private Problem problem;

    @Column(columnDefinition = "TEXT")
    private String intuition;

    @Column(name = "brute_force", columnDefinition = "TEXT")
    private String bruteForce;

    @Column(name = "brute_time")
    private String bruteTime;

    @Column(name = "brute_space")
    private String bruteSpace;

    @Column(name = "optimal_approach", columnDefinition = "TEXT")
    private String optimalApproach;

    @Column(name = "optimal_time")
    private String optimalTime;

    @Column(name = "optimal_space")
    private String optimalSpace;

    @Column(columnDefinition = "TEXT")
    private String pseudocode;

    @Column(name = "java_solution", columnDefinition = "TEXT")
    private String javaSolution;

    @Column(name = "common_mistakes", columnDefinition = "TEXT")
    private String commonMistakes;

    @Column(name = "recognition_note", columnDefinition = "TEXT")
    private String recognitionNote;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
