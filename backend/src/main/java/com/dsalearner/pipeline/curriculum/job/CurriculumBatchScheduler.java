package com.dsalearner.pipeline.curriculum.job;

import com.dsalearner.pipeline.model.entity.CfCurriculum;
import com.dsalearner.pipeline.repository.CfCurriculumRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Periodic scheduler that drives curriculum content generation.
 * Delegates all transactional work to CurriculumBatchProcessor to ensure
 * @Transactional is applied via Spring AOP proxy.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CurriculumBatchScheduler {

    private final CfCurriculumRepository curriculumRepository;
    private final CurriculumBatchProcessor batchProcessor;

    @Scheduled(fixedDelay = 30_000)
    public void tick() {
        List<CfCurriculum> active = curriculumRepository.findByCurriculumStatus("GENERATION_IN_PROGRESS");
        for (CfCurriculum curriculum : active) {
            try {
                batchProcessor.processCurriculum(curriculum);
            } catch (Exception e) {
                log.error("CurriculumBatchScheduler: error processing curriculum {} — {}",
                        curriculum.getId(), e.getMessage(), e);
            }
        }
    }
}
