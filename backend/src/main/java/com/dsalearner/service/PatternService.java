package com.dsalearner.service;

import com.dsalearner.dto.response.PatternResponse;
import com.dsalearner.exception.NotFoundException;
import com.dsalearner.model.entity.Pattern;
import com.dsalearner.model.entity.PatternMastery;
import com.dsalearner.repository.PatternMasteryRepository;
import com.dsalearner.repository.PatternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatternService {

    private final PatternRepository patternRepository;
    private final PatternMasteryRepository masteryRepository;

    public List<PatternResponse> findAll() {
        return patternRepository.findAll(Sort.by("displayOrder"))
                .stream().map(p -> toResponse(p, null)).toList();
    }

    public List<PatternResponse> findAllByCategory(String category) {
        return patternRepository.findByCategoryOrderByDisplayOrder(category)
                .stream().map(p -> toResponse(p, null)).toList();
    }

    public List<PatternResponse> findAllForUser(UUID userId) {
        List<Pattern> patterns = patternRepository.findAll(Sort.by("displayOrder"));
        List<PatternMastery> masteries = masteryRepository.findByUserId(userId);
        Map<UUID, String> masteryMap = masteries.stream()
                .collect(Collectors.toMap(m -> m.getPattern().getId(), m -> m.getStatus().name()));
        return patterns.stream()
                .map(p -> toResponse(p, masteryMap.getOrDefault(p.getId(), "NOT_STARTED")))
                .toList();
    }

    public List<PatternResponse> findAllByCategoryForUser(String category, UUID userId) {
        List<Pattern> patterns = patternRepository.findByCategoryOrderByDisplayOrder(category);
        List<PatternMastery> masteries = masteryRepository.findByUserId(userId);
        Map<UUID, String> masteryMap = masteries.stream()
                .collect(Collectors.toMap(m -> m.getPattern().getId(), m -> m.getStatus().name()));
        return patterns.stream()
                .map(p -> toResponse(p, masteryMap.getOrDefault(p.getId(), "NOT_STARTED")))
                .toList();
    }

    public PatternResponse findBySlug(String slug) {
        return patternRepository.findBySlug(slug)
                .map(p -> toResponse(p, null))
                .orElseThrow(() -> new NotFoundException("Pattern not found: " + slug));
    }

    public PatternResponse findBySlugForUser(String slug, UUID userId) {
        Pattern p = patternRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Pattern not found: " + slug));
        String status = masteryRepository.findByUserIdAndPatternId(userId, p.getId())
                .map(m -> m.getStatus().name())
                .orElse("NOT_STARTED");
        return toResponse(p, status);
    }

    private PatternResponse toResponse(Pattern p, String masteryStatus) {
        List<String> clues = p.getRecognitionClues() == null ? List.of()
                : Arrays.asList(p.getRecognitionClues().split("\n"));
        return new PatternResponse(p.getId(), p.getSlug(), p.getName(), p.getSummary(),
                clues, p.getTemplateCode(), p.getDisplayOrder(),
                p.getLessonMarkdown(), p.getTimeComplexity(), p.getSpaceComplexity(),
                masteryStatus, p.getCategory());
    }
}
