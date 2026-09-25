package com.dsalearner.pipeline.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "cf_agent_runs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfAgentRun {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID lessonId;

    @Column(nullable = false)
    private int lessonVersion;

    @Column(nullable = false, length = 60)
    private String agentType;

    @Column(nullable = false, length = 20)
    private String domainCode;

    @Column(length = 10)
    private String languageCode;

    private UUID jobId;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "QUEUED";

    @Column(length = 64)
    private String inputHash;

    private UUID promptId;
    private Integer promptVersion;

    @Column(length = 100)
    private String modelConfigKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Object output;

    @Column(precision = 4, scale = 3)
    private BigDecimal confidence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Object issues;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Object recommendations;

    @Column(nullable = false)
    @Builder.Default
    private boolean culturalFlag = false;

    // Cost tracking
    private Integer inputTokens;
    private Integer outputTokens;

    @Column(precision = 10, scale = 6)
    private BigDecimal estimatedCostUsd;

    @Column(length = 50)
    private String provider;

    @Column(length = 100)
    private String modelId;

    private Long latencyMs;

    @Column(nullable = false)
    @Builder.Default
    private int retryCount = 0;

    @Column(columnDefinition = "text")
    private String errorMessage;

    private Instant startedAt;
    private Instant completedAt;

    @CreationTimestamp
    private Instant createdAt;
}
