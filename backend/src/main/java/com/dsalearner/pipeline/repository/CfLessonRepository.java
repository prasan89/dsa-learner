package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.model.entity.CfLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CfLessonRepository extends JpaRepository<CfLesson, UUID> {
    Optional<CfLesson> findByStableRef(String stableRef);
    List<CfLesson> findByDomainCodeAndContentStatus(String domainCode, ContentStatus status);
    List<CfLesson> findByLanguageCodeAndContentStatus(String languageCode, ContentStatus status);
}
