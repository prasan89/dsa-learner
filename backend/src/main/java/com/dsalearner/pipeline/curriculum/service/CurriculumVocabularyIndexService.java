package com.dsalearner.pipeline.curriculum.service;

import com.dsalearner.pipeline.model.entity.CfCurriculumVocabularyIndex;
import com.dsalearner.pipeline.repository.CfCurriculumVocabularyIndexRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages the vocabulary term index for a curriculum.
 * Tracks when each term is introduced, reviewed, and which plans depend on it.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumVocabularyIndexService {

    private final CfCurriculumVocabularyIndexRepository vocabIndexRepository;

    public List<CfCurriculumVocabularyIndex> getAll(UUID curriculumId) {
        return vocabIndexRepository.findByCurriculumId(curriculumId);
    }

    public List<CfCurriculumVocabularyIndex> getForLevel(UUID curriculumId, String cefrLevel) {
        return vocabIndexRepository.findByCurriculumIdAndCefrLevel(curriculumId, cefrLevel);
    }

    /**
     * Upsert a vocabulary term entry.
     */
    @Transactional
    public CfCurriculumVocabularyIndex upsert(UUID curriculumId,
                                               String languageCode,
                                               String term,
                                               String cefrLevel,
                                               UUID introductionPlanId,
                                               String[] relatedTerms) {
        Optional<CfCurriculumVocabularyIndex> existing =
                vocabIndexRepository.findByCurriculumIdAndLanguageCodeAndTerm(curriculumId, languageCode, term);

        if (existing.isPresent()) {
            CfCurriculumVocabularyIndex entry = existing.get();
            if (entry.getIntroductionPlanId() == null && introductionPlanId != null) {
                entry.setIntroductionPlanId(introductionPlanId);
            }
            if (relatedTerms != null) {
                entry.setRelatedTerms(relatedTerms);
            }
            return vocabIndexRepository.save(entry);
        }

        CfCurriculumVocabularyIndex entry = CfCurriculumVocabularyIndex.builder()
                .curriculumId(curriculumId)
                .languageCode(languageCode)
                .term(term)
                .cefrLevel(cefrLevel)
                .introductionPlanId(introductionPlanId)
                .relatedTerms(relatedTerms)
                .build();
        return vocabIndexRepository.save(entry);
    }

    /**
     * Append a review plan for a vocabulary term.
     */
    @Transactional
    public void addReview(UUID curriculumId, String languageCode, String term, UUID planId) {
        vocabIndexRepository
                .findByCurriculumIdAndLanguageCodeAndTerm(curriculumId, languageCode, term)
                .ifPresent(entry -> {
                    UUID[] updated = appendUuid(entry.getReviewPlanIds(), planId);
                    entry.setReviewPlanIds(updated);
                    vocabIndexRepository.save(entry);
                });
    }

    /**
     * Append a dependent plan (a lesson that depends on this vocabulary).
     */
    @Transactional
    public void addDependent(UUID curriculumId, String languageCode, String term, UUID planId) {
        vocabIndexRepository
                .findByCurriculumIdAndLanguageCodeAndTerm(curriculumId, languageCode, term)
                .ifPresent(entry -> {
                    UUID[] updated = appendUuid(entry.getDependentPlanIds(), planId);
                    entry.setDependentPlanIds(updated);
                    vocabIndexRepository.save(entry);
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
