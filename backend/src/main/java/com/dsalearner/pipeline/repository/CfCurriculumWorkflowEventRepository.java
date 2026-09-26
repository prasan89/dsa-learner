package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfCurriculumWorkflowEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CfCurriculumWorkflowEventRepository extends JpaRepository<CfCurriculumWorkflowEvent, Long> {

    List<CfCurriculumWorkflowEvent> findByCurriculumIdOrderByOccurredAtDesc(UUID curriculumId);

    List<CfCurriculumWorkflowEvent> findByLevelIdOrderByOccurredAtDesc(UUID levelId);
}
