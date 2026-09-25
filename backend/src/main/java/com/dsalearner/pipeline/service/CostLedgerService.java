package com.dsalearner.pipeline.service;

import com.dsalearner.pipeline.model.entity.CfCostLedger;
import com.dsalearner.pipeline.repository.CfCostLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CostLedgerService {

    private final CfCostLedgerRepository costLedgerRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID lessonId, int lessonVersion, UUID agentRunId,
                       String domainCode, String languageCode,
                       String provider, String modelId,
                       int inputTokens, int outputTokens, double costUsd,
                       String budgetKey) {

        CfCostLedger entry = CfCostLedger.builder()
                .lessonId(lessonId)
                .lessonVersion(lessonVersion)
                .agentRunId(agentRunId)
                .domainCode(domainCode)
                .languageCode(languageCode)
                .provider(provider)
                .modelId(modelId)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .costUsd(BigDecimal.valueOf(costUsd))
                .budgetKey(budgetKey)
                .recordedAt(Instant.now())
                .build();

        costLedgerRepository.save(entry);
        log.debug("Cost recorded: lessonId={} agentRun={} cost=${} provider={}",
                lessonId, agentRunId, costUsd, provider);
    }

    public BigDecimal totalCostForLesson(UUID lessonId) {
        return costLedgerRepository.sumCostByLessonId(lessonId);
    }

    public BigDecimal todayTotalCost() {
        return costLedgerRepository.sumTodayCost();
    }
}
