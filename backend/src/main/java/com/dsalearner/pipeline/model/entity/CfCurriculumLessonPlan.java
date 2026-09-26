package com.dsalearner.pipeline.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cf_curriculum_lesson_plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfCurriculumLessonPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID curriculumId;

    @Column(nullable = false)
    private UUID levelId;

    private UUID unitId;

    @Column(nullable = false, unique = true, length = 100)
    private String stableRef;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String topic;

    @Column(nullable = false, length = 40)
    @Builder.Default
    private String lessonType = "LEARN";

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String difficulty = "FOUNDATION";

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] skillFocus;

    @Column(columnDefinition = "text")
    private String learningObjectives;

    @Column(columnDefinition = "text")
    private String communicationGoals;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] grammarTargets;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] vocabTargets;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String planStatus = "PLANNED";

    private UUID lessonId;

    @Column(length = 200)
    private String unitDisplayName;

    @Column(nullable = false)
    @Builder.Default
    private int generationAttempt = 0;

    @Column(columnDefinition = "text")
    private String lastError;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    private Instant updatedAt;
}
