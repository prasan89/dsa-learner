package com.dsalearner.pipeline.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cf_curriculum_units")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfCurriculumUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID levelId;

    @Column(nullable = false)
    private UUID curriculumId;

    @Column(nullable = false)
    private int ordinal;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(columnDefinition = "text")
    private String theme;

    @Column(columnDefinition = "text")
    private String learningGoal;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
