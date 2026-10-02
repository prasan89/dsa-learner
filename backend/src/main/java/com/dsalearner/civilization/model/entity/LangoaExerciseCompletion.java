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

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID lessonId;

    @Column(nullable = false, length = 64)
    private String exerciseId;

    @Column(nullable = false, length = 10)
    private String languageCode;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant completedAt = Instant.now();
}
