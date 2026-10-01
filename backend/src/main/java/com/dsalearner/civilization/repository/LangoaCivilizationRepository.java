package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.model.entity.LangoaCivilization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LangoaCivilizationRepository extends JpaRepository<LangoaCivilization, UUID> {

    Optional<LangoaCivilization> findByUserIdAndLanguageCode(UUID userId, String languageCode);
}
