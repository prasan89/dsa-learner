package com.dsalearner.service;

import com.dsalearner.dto.response.PatternResponse;
import com.dsalearner.exception.NotFoundException;
import com.dsalearner.model.entity.Pattern;
import com.dsalearner.repository.PatternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatternService {

    private final PatternRepository patternRepository;

    public List<PatternResponse> findAll() {
        return patternRepository.findAll(Sort.by("displayOrder"))
                .stream().map(this::toResponse).toList();
    }

    public PatternResponse findBySlug(String slug) {
        return patternRepository.findBySlug(slug)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Pattern not found: " + slug));
    }

    private PatternResponse toResponse(Pattern p) {
        List<String> clues = p.getRecognitionClues() == null ? List.of()
                : Arrays.asList(p.getRecognitionClues().split("\n"));
        return new PatternResponse(p.getId(), p.getSlug(), p.getName(), p.getSummary(),
                clues, p.getTemplateCode(), p.getDisplayOrder());
    }
}
