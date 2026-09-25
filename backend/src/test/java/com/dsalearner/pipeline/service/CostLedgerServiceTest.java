package com.dsalearner.pipeline.service;

import com.dsalearner.pipeline.model.entity.CfCostLedger;
import com.dsalearner.pipeline.repository.CfCostLedgerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CostLedgerServiceTest {

    @Mock CfCostLedgerRepository repo;
    @InjectMocks CostLedgerService service;

    @Test
    void recordsSavesCorrectEntry() {
        UUID lessonId = UUID.randomUUID();
        UUID runId    = UUID.randomUUID();
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.record(lessonId, 1, runId, "language", "de",
                "anthropic", "claude-haiku-4-5", 1000, 500, 0.0015,
                "per_lesson:" + lessonId);

        ArgumentCaptor<CfCostLedger> captor = ArgumentCaptor.forClass(CfCostLedger.class);
        verify(repo).save(captor.capture());
        CfCostLedger entry = captor.getValue();

        assertEquals(lessonId, entry.getLessonId());
        assertEquals(runId,    entry.getAgentRunId());
        assertEquals("language", entry.getDomainCode());
        assertEquals("de",       entry.getLanguageCode());
        assertEquals(1000, entry.getInputTokens());
        assertEquals(500,  entry.getOutputTokens());
        assertEquals(0, BigDecimal.valueOf(0.0015).compareTo(entry.getCostUsd()));
    }

    @Test
    void totalCostDelegatesToRepo() {
        UUID lessonId = UUID.randomUUID();
        when(repo.sumCostByLessonId(lessonId)).thenReturn(new BigDecimal("0.370000"));
        assertEquals(new BigDecimal("0.370000"), service.totalCostForLesson(lessonId));
    }

    @Test
    void todayCostDelegatesToRepo() {
        when(repo.sumTodayCost()).thenReturn(new BigDecimal("1.230000"));
        assertEquals(new BigDecimal("1.230000"), service.todayTotalCost());
    }
}
