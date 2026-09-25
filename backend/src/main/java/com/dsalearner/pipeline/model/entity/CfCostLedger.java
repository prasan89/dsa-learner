package com.dsalearner.pipeline.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cf_cost_ledger")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfCostLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID lessonId;
    private Integer lessonVersion;
    private UUID agentRunId;

    @Column(length = 20)
    private String domainCode;

    @Column(length = 10)
    private String languageCode;

    @Column(length = 50)
    private String provider;

    @Column(length = 100)
    private String modelId;

    private Integer inputTokens;
    private Integer outputTokens;

    @Column(precision = 10, scale = 6)
    private BigDecimal costUsd;

    @Column(length = 100)
    private String budgetKey;

    @Column(nullable = false)
    @Builder.Default
    private Instant recordedAt = Instant.now();
}
