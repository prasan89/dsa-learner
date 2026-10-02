package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.model.entity.LangoaExerciseCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LangoaExerciseCompletionRepository extends JpaRepository<LangoaExerciseCompletion, UUID> {
    boolean existsByUserIdAndExerciseIdAndLanguageCode(UUID userId, String exerciseId, String languageCode);
}
