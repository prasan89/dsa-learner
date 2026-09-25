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
@Table(name = "cf_language_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfLanguageProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    private CfDomain domain;

    @Column(nullable = false, unique = true, length = 10)
    private String languageCode;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(length = 30)
    private String script;

    @Column(nullable = false)
    @Builder.Default
    private boolean cefrApplicable = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean rtl = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(length = 60)
    private String linguisticQaAgent;

    @Column(length = 500)
    private String charValidationRegex;

    @Column(length = 200)
    private String styleGuideRef;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> promptIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> qaThresholds;

    @CreationTimestamp
    private Instant createdAt;
}
