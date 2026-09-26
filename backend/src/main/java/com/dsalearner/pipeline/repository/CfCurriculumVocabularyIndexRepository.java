package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfCurriculumVocabularyIndex;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CfCurriculumVocabularyIndexRepository extends JpaRepository<CfCurriculumVocabularyIndex, UUID> {

    Optional<CfCurriculumVocabularyIndex> findByCurriculumIdAndLanguageCodeAndTerm(
            UUID curriculumId, String languageCode, String term);

    List<CfCurriculumVocabularyIndex> findByCurriculumId(UUID curriculumId);

    List<CfCurriculumVocabularyIndex> findByCurriculumIdAndCefrLevel(UUID curriculumId, String cefrLevel);
}
