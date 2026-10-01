package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.model.entity.LangoaAchievementProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LangoaAchievementProgressRepository extends JpaRepository<LangoaAchievementProgress, UUID> {
    List<LangoaAchievementProgress> findByUserIdAndLanguageCode(UUID userId, String languageCode);
    Optional<LangoaAchievementProgress> findByUserIdAndLanguageCodeAndAchievementId(
            UUID userId, String languageCode, UUID achievementId);
}
