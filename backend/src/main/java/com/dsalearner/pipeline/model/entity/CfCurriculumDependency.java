package com.dsalearner.pipeline.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cf_curriculum_dependencies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfCurriculumDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID curriculumId;

    @Column(nullable = false)
    private UUID lessonPlanId;

    @Column(nullable = false)
    private UUID requiredPlanId;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String dependencyType = "PREREQUISITE";

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
