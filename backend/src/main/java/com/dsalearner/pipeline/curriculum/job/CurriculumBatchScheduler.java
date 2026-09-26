package com.dsalearner.pipeline.curriculum.job;

import com.dsalearner.pipeline.model.entity.CfCurriculum;
import com.dsalearner.pipeline.model.entity.CfCurriculumLevel;
import com.dsalearner.pipeline.repository.CfCurriculumRepository;
import com.dsalearner.pipeline.repository.CfCurriculumLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Periodic scheduler that drives curriculum content generation.
 * Delegates all transactional work to CurriculumBatchProcessor to ensure
 * @Transactional is applied via Spring AOP proxy.
 *
 * sweepQaPending is called OUTSIDE processCurriculum's transaction so that
 * a conflict exception in submitQaContent cannot poison the dispatch transaction.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CurriculumBatchScheduler {

    private final CfCurriculumRepository curriculumRepository;
    private final CfCurriculumLevelRepository levelRepository;
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

            // Sweep runs outside the main transaction to prevent conflict exceptions
            // from submitQaContent poisoning the dispatch transaction.
            List<CfCurriculumLevel> generatingLevels = levelRepository
                    .findByCurriculumIdAndLevelStatusIn(curriculum.getId(), List.of("GENERATION_IN_PROGRESS"));
            for (CfCurriculumLevel level : generatingLevels) {
                try {
                    batchProcessor.sweepQaPending(curriculum, level);
                } catch (Exception e) {
                    log.warn("CurriculumBatchScheduler: sweep error level {} — {}",
                            level.getCefrLevel(), e.getMessage());
                }
            }
        }
    }
}
