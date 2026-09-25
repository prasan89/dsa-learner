package com.dsalearner.pipeline.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cf_ai_model_configs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfAiModelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(nullable = false, length = 50)
    private String provider;

    @Column(nullable = false, length = 100)
    private String modelId;

    @Column(nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal temperature = new BigDecimal("0.30");

    @Column(nullable = false)
    @Builder.Default
    private int maxTokens = 4096;

    @Column(nullable = false)
    @Builder.Default
    private int timeoutMs = 30000;

    @Column(nullable = false, precision = 8, scale = 6)
    @Builder.Default
    private BigDecimal costPer1kInputUsd = BigDecimal.ZERO;

    @Column(nullable = false, precision = 8, scale = 6)
    @Builder.Default
    private BigDecimal costPer1kOutputUsd = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    private Instant createdAt;
}
