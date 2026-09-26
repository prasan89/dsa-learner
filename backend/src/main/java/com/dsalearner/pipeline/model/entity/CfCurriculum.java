package com.dsalearner.pipeline.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cf_curricula")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfCurriculum {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String stableRef;

    @Column(nullable = false, length = 20)
    private String domainCode;

    @Column(nullable = false, length = 10)
    private String languageCode;

    @Column(nullable = false, length = 200)
    private String displayName;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String curriculumStatus = "DRAFT";

    @Column(nullable = false)
    @Builder.Default
    private int curriculumVersion = 1;

    private Integer activeVersion;

    @Column(nullable = false)
    @Builder.Default
    private boolean publishGatePassed = false;

    @Column(nullable = false)
    @Builder.Default
    private int batchSize = 10;

    @Column(length = 100)
    private String createdBy;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    private Instant updatedAt;
}
