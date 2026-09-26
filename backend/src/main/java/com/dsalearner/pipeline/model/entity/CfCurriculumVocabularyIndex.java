package com.dsalearner.pipeline.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cf_curriculum_vocabulary_index")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfCurriculumVocabularyIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID curriculumId;

    @Column(nullable = false, length = 100)
    private String term;

    @Column(nullable = false, length = 10)
    private String languageCode;

    @Column(nullable = false, length = 4)
    private String cefrLevel;

    private UUID introductionPlanId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "uuid[]")
    private UUID[] reviewPlanIds;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] relatedTerms;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "uuid[]")
    private UUID[] dependentPlanIds;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    private Instant updatedAt;
}
