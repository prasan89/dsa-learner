package com.dsalearner.civilization.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "langoa_exercise_completions",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_exercise_completion",
        columnNames = {"user_id", "exercise_id", "language_code"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LangoaExerciseCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "lesson_id", nullable = false)
    private UUID lessonId;

    @Column(name = "exercise_id", nullable = false, length = 64)
    private String exerciseId;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "completed_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant completedAt = Instant.now();
}
