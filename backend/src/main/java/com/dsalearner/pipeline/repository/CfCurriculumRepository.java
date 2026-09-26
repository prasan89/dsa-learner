package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfCurriculum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CfCurriculumRepository extends JpaRepository<CfCurriculum, UUID> {

    Optional<CfCurriculum> findByStableRef(String stableRef);

    List<CfCurriculum> findByDomainCodeAndLanguageCode(String domainCode, String languageCode);

    List<CfCurriculum> findByLanguageCode(String languageCode);

    List<CfCurriculum> findByCurriculumStatus(String curriculumStatus);

    List<CfCurriculum> findByCurriculumStatusIn(List<String> statuses);
}
