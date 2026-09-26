package com.dsalearner.pipeline.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cf_curriculum_levels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfCurriculumLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID curriculumId;

    @Column(nullable = false, length = 4)
    private String cefrLevel;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false)
    private int ordinal;

    private Integer targetLessonCount;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String levelStatus = "PLANNED";

    private UUID blueprintAgentRunId;

    private UUID levelQaRunId;

    @Column(nullable = false)
    @Builder.Default
    private boolean humanReviewFlag = false;

    @Column(columnDefinition = "text")
    private String humanReviewReason;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    private Instant updatedAt;
}
