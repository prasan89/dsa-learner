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
@Table(name = "cf_lesson_versions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfLessonVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID lessonId;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false, length = 30)
    private String contentStatus;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String publicationStatus = "UNPUBLISHED";

    // Content blobs
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> blueprint;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> vocabulary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> grammar;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> exercises;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> audioManifest;

    // Lineage (populated during pipeline execution)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> promptVersions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> modelConfigs;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> generatorRunIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> qaRunIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> revisionLog;

    // Added in Phase 1B (V36): structured QA issues forwarded to the revision generator.
    // Populated on QA FAIL so the next generation agent receives targeted feedback.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> revisionFeedback;

    // Added in Phase 1B.1 (V37): version number of the version that was revised to produce this one.
    // NULL for originally-generated versions.
    @Column
    private Integer parentVersion;

    @Column(nullable = false)
    @Builder.Default
    private boolean frozen = false;

    @Column(length = 64)
    private String checksum;

    private Instant publishedAt;
    private Instant supersededAt;

    @CreationTimestamp
    private Instant createdAt;
}
