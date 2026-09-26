package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfCurriculumGrammarIndex;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CfCurriculumGrammarIndexRepository extends JpaRepository<CfCurriculumGrammarIndex, UUID> {

    Optional<CfCurriculumGrammarIndex> findByCurriculumIdAndConceptKey(UUID curriculumId, String conceptKey);

    List<CfCurriculumGrammarIndex> findByCurriculumId(UUID curriculumId);

    List<CfCurriculumGrammarIndex> findByCurriculumIdAndCefrLevel(UUID curriculumId, String cefrLevel);
}
