package com.dsalearner.pipeline.model.entity;

import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.domain.PublicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cf_lessons")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CfLesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String stableRef;

    @Column(length = 255)
    private String title;

    @Column(nullable = false, length = 20)
    private String domainCode;

    @Column(length = 10)
    private String languageCode;

    @Column(length = 2)
    private String cefrLevel;

    @Column(columnDefinition = "text[]")
    private String[] skillFocus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ContentStatus contentStatus = ContentStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PublicationStatus publicationStatus = PublicationStatus.UNPUBLISHED;

    @Column(nullable = false)
    @Builder.Default
    private int currentVersion = 1;

    private Integer activeVersion;

    @Column(nullable = false)
    @Builder.Default
    private int revisionCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private int maxRevisionAttempts = 3;

    @Column(nullable = false)
    @Builder.Default
    private boolean humanReviewFlag = false;

    @Column(columnDefinition = "text")
    private String humanReviewReason;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
