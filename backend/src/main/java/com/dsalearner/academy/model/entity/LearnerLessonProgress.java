package com.dsalearner.academy.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "learner_lesson_progress")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LearnerLessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID lessonId;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "NOT_STARTED";

    @Column(nullable = false)
    @Builder.Default
    private int stepIndex = 0;

    private Short score;

    private Instant startedAt;
    private Instant completedAt;
    private Instant lastInteractionAt;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    private Instant updatedAt;
}
