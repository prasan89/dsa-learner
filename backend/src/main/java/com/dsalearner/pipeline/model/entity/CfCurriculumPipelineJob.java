package com.dsalearner.pipeline.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "cf_curriculum_pipeline_jobs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfCurriculumPipelineJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID curriculumId;

    @Column
    private UUID levelId;

    @Column(nullable = false, length = 50)
    private String jobType;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "QUEUED";

    @Column(nullable = false)
    @Builder.Default
    private int attempt = 0;

    @Column(nullable = false)
    @Builder.Default
    private int maxAttempts = 3;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Object> payload = java.util.Map.of();

    @Column(length = 200)
    private String resultReference;

    @Column(columnDefinition = "text")
    private String error;

    @CreationTimestamp
    private Instant createdAt;

    private Instant startedAt;

    private Instant completedAt;
}
