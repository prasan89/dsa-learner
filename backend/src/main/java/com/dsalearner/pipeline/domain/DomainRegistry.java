package com.dsalearner.pipeline.domain;

import com.dsalearner.pipeline.exception.DomainNotFoundException;
import com.dsalearner.pipeline.model.entity.CfDomain;
import com.dsalearner.pipeline.model.entity.CfLanguageProfile;
import com.dsalearner.pipeline.repository.CfDomainRepository;
import com.dsalearner.pipeline.repository.CfLanguageProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves DomainPlugin and LanguageProfile from database.
 * Acts as the single source of truth for domain/language configuration.
 */
@Component
@RequiredArgsConstructor
public class DomainRegistry {

    private final CfDomainRepository domainRepository;
    private final CfLanguageProfileRepository languageProfileRepository;

    public DomainPlugin getDomain(String domainCode) {
        CfDomain entity = domainRepository.findByCode(domainCode)
                .orElseThrow(() -> new DomainNotFoundException("Domain not found: " + domainCode));
        return toDomainPlugin(entity);
    }

    public Optional<LanguageProfile> findLanguageProfile(String languageCode) {
        return languageProfileRepository.findByLanguageCode(languageCode)
                .map(this::toLanguageProfile);
    }

    public LanguageProfile getLanguageProfile(String languageCode) {
        return findLanguageProfile(languageCode)
                .orElseThrow(() -> new DomainNotFoundException("Language profile not found: " + languageCode));
    }

    public List<LanguageProfile> activeProfiles() {
        return languageProfileRepository.findByActiveTrue()
                .stream().map(this::toLanguageProfile).toList();
    }

    private DomainPlugin toDomainPlugin(CfDomain entity) {
        @SuppressWarnings("unchecked")
        List<String> agents = entity.getPluginConfig() != null
                ? (List<String>) ((Map<String, Object>) entity.getPluginConfig()).getOrDefault("activeAgents", List.of())
                : List.of();
        return new DomainPlugin(
                entity.getCode(),
                entity.getDisplayName(),
                entity.isActive(),
                agents,
                entity.getPluginConfig() != null ? entity.getPluginConfig() : Map.of()
        );
    }

    @SuppressWarnings("unchecked")
    private LanguageProfile toLanguageProfile(CfLanguageProfile entity) {
        Map<String, String> promptIds = entity.getPromptIds() != null
                ? (Map<String, String>) (Map<?, ?>) entity.getPromptIds()
                : Map.of();
        Map<String, Double> qaThresholds = entity.getQaThresholds() != null
                ? (Map<String, Double>) (Map<?, ?>) entity.getQaThresholds()
                : Map.of();
        return new LanguageProfile(
                entity.getLanguageCode(),
                entity.getDisplayName(),
                entity.getDomain().getCode(),
                entity.isCefrApplicable(),
                entity.isRtl(),
                entity.isActive(),
                entity.getLinguisticQaAgent(),
                entity.getCharValidationRegex(),
                promptIds,
                qaThresholds
        );
    }
}
