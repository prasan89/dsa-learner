package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfWorkflowEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CfWorkflowEventRepository extends JpaRepository<CfWorkflowEvent, Long> {
    List<CfWorkflowEvent> findByLessonIdOrderByOccurredAtDesc(UUID lessonId);
}
