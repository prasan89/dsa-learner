package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfLessonVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CfLessonVersionRepository extends JpaRepository<CfLessonVersion, UUID> {
    Optional<CfLessonVersion> findByLessonIdAndVersion(UUID lessonId, int version);
    Optional<CfLessonVersion> findByLessonIdAndVersionAndFrozenTrue(UUID lessonId, int version);
    List<CfLessonVersion> findAllByLessonIdOrderByVersionAsc(UUID lessonId);
}
