package com.dsalearner.pipeline.curriculum.service;

import com.dsalearner.pipeline.model.entity.CfCurriculumGrammarIndex;
import com.dsalearner.pipeline.repository.CfCurriculumGrammarIndexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages the grammar concept index for a curriculum.
 * Records which plan introduces each grammar concept and tracks
 * prerequisites + reinforcement plans.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumGrammarIndexService {

    private final CfCurriculumGrammarIndexRepository grammarIndexRepository;

    public List<CfCurriculumGrammarIndex> getAll(UUID curriculumId) {
        return grammarIndexRepository.findByCurriculumId(curriculumId);
    }

    public List<CfCurriculumGrammarIndex> getForLevel(UUID curriculumId, String cefrLevel) {
        return grammarIndexRepository.findByCurriculumIdAndCefrLevel(curriculumId, cefrLevel);
    }

    /**
     * Upsert a grammar concept entry.
     * If the concept already exists, merges reinforcement plan IDs.
     */
    @Transactional
    public CfCurriculumGrammarIndex upsert(UUID curriculumId,
                                            String conceptKey,
                                            String displayName,
                                            String cefrLevel,
                                            UUID introductionPlanId,
                                            String[] prerequisiteConceptKeys) {
        Optional<CfCurriculumGrammarIndex> existing =
                grammarIndexRepository.findByCurriculumIdAndConceptKey(curriculumId, conceptKey);

        if (existing.isPresent()) {
            CfCurriculumGrammarIndex entry = existing.get();
            // Only update intro plan if not yet set
            if (entry.getIntroductionPlanId() == null && introductionPlanId != null) {
                entry.setIntroductionPlanId(introductionPlanId);
            }
            if (prerequisiteConceptKeys != null) {
                entry.setPrerequisiteConceptKeys(prerequisiteConceptKeys);
            }
            return grammarIndexRepository.save(entry);
        }

        CfCurriculumGrammarIndex entry = CfCurriculumGrammarIndex.builder()
                .curriculumId(curriculumId)
                .conceptKey(conceptKey)
                .displayName(displayName != null ? displayName : conceptKey)
                .cefrLevel(cefrLevel)
                .introductionPlanId(introductionPlanId)
                .prerequisiteConceptKeys(prerequisiteConceptKeys)
                .build();
        return grammarIndexRepository.save(entry);
    }

    /**
     * Append a reinforcement plan for an already-introduced grammar concept.
     */
    @Transactional
    public void addReinforcement(UUID curriculumId, String conceptKey, UUID planId) {
        grammarIndexRepository.findByCurriculumIdAndConceptKey(curriculumId, conceptKey)
                .ifPresent(entry -> {
                    UUID[] current = entry.getReinforcementPlanIds();
                    UUID[] updated = appendUuid(current, planId);
                    entry.setReinforcementPlanIds(updated);
                    grammarIndexRepository.save(entry);
                });
    }

    /**
     * Append a dependent plan (a lesson that uses this grammar concept).
     */
    @Transactional
    public void addDependent(UUID curriculumId, String conceptKey, UUID planId) {
        grammarIndexRepository.findByCurriculumIdAndConceptKey(curriculumId, conceptKey)
                .ifPresent(entry -> {
                    UUID[] updated = appendUuid(entry.getDependentPlanIds(), planId);
                    entry.setDependentPlanIds(updated);
                    grammarIndexRepository.save(entry);
                });
    }

    private UUID[] appendUuid(UUID[] existing, UUID toAdd) {
        if (existing == null) return new UUID[]{toAdd};
        for (UUID u : existing) {
            if (u.equals(toAdd)) return existing;
        }
        UUID[] result = Arrays.copyOf(existing, existing.length + 1);
        result[existing.length] = toAdd;
        return result;
    }
}
