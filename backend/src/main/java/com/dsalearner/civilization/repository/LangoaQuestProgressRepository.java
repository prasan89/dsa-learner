package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.model.entity.LangoaQuestProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LangoaQuestProgressRepository extends JpaRepository<LangoaQuestProgress, UUID> {
    List<LangoaQuestProgress> findByUserIdAndLanguageCodeAndQuestDate(
            UUID userId, String languageCode, LocalDate questDate);

    Optional<LangoaQuestProgress> findByUserIdAndLanguageCodeAndQuestIdAndQuestDate(
            UUID userId, String languageCode, UUID questId, LocalDate questDate);
}
