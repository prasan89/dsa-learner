package com.dsalearner.pipeline.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cf_curriculum_grammar_index")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfCurriculumGrammarIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID curriculumId;

    @Column(nullable = false, length = 100)
    private String conceptKey;

    @Column(nullable = false, length = 200)
    private String displayName;

    @Column(nullable = false, length = 4)
    private String cefrLevel;

    private UUID introductionPlanId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] prerequisiteConceptKeys;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "uuid[]")
    private UUID[] reinforcementPlanIds;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "uuid[]")
    private UUID[] dependentPlanIds;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    private Instant updatedAt;
}
