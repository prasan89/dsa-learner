package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfLanguageProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CfLanguageProfileRepository extends JpaRepository<CfLanguageProfile, UUID> {
    Optional<CfLanguageProfile> findByLanguageCode(String languageCode);
    List<CfLanguageProfile> findByActiveTrue();
}
