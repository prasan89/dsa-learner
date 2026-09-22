package com.dsalearner.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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

    // ── 3-level explanation ──────────────────────────────────────────────────
    @Column(columnDefinition = "TEXT")
    private String intuition;

    @Column(name = "guided_reasoning", columnDefinition = "TEXT")
    private String guidedReasoning;

    @Column(columnDefinition = "TEXT")
    private String solution;

    // ── Pattern recognition ──────────────────────────────────────────────────
    @Column(name = "recognition_note", columnDefinition = "TEXT")
    private String recognitionNote;

    @Column(name = "pattern_recognition_clues", columnDefinition = "TEXT")
    private String patternRecognitionClues;

    @Column(name = "when_to_use", columnDefinition = "TEXT")
    private String whenToUse;

    @Column(name = "when_not_to_use", columnDefinition = "TEXT")
    private String whenNotToUse;

    // ── Approach ─────────────────────────────────────────────────────────────
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

    // ── Legacy column kept for backwards compatibility ───────────────────────
    @Column(name = "java_solution", columnDefinition = "TEXT")
    private String javaSolution;

    // ── Why this works ───────────────────────────────────────────────────────
    @Column(name = "why_this_works", columnDefinition = "TEXT")
    private String whyThisWorks;

    @Column(columnDefinition = "TEXT")
    private String invariant;

    // ── Mistakes & senior track ──────────────────────────────────────────────
    @Column(name = "common_mistakes", columnDefinition = "TEXT")
    private String commonMistakes;

    @Column(name = "senior_variations", columnDefinition = "TEXT")
    private String seniorVariations;

    @Column(name = "content_status", length = 30, nullable = false)
    @Builder.Default
    private String contentStatus = "DRAFT";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "quality_flags", columnDefinition = "jsonb")
    private java.util.Map<String, Boolean> qualityFlags;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
