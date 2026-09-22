package com.dsalearner.service;

import com.dsalearner.dto.response.PageResponse;
import com.dsalearner.dto.response.ProblemResponse;
import com.dsalearner.dto.response.ProblemSummaryResponse;
import com.dsalearner.exception.NotFoundException;
import com.dsalearner.model.entity.Problem;
import com.dsalearner.model.entity.ProblemContent;
import com.dsalearner.model.enums.Difficulty;
import com.dsalearner.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository         problemRepository;
    private final UserProgressRepository    userProgressRepository;
    private final HintRepository            hintRepository;
    private final ProblemContentRepository  problemContentRepository;
    private final ProblemFollowupRepository problemFollowupRepository;

    public PageResponse<ProblemSummaryResponse> findAll(String difficulty, String patternId,
                                                         int page, int size, UUID userId) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("title"));
        Page<Problem> result;

        if (difficulty != null && patternId != null) {
            result = problemRepository.findAllByActiveTrueAndPatternIdAndDifficulty(
                    UUID.fromString(patternId), Difficulty.valueOf(difficulty.toUpperCase()), pageable);
        } else if (patternId != null) {
            result = problemRepository.findAllByActiveTrueAndPatternId(UUID.fromString(patternId), pageable);
        } else if (difficulty != null) {
            result = problemRepository.findAllByActiveTrueAndDifficulty(
                    Difficulty.valueOf(difficulty.toUpperCase()), pageable);
        } else {
            result = problemRepository.findAllByActiveTrue(pageable);
        }

        Set<UUID> solvedIds = userId == null ? Set.of()
                : userProgressRepository.findSolvedProblemIdsByUserId(userId);

        List<ProblemSummaryResponse> content = result.getContent().stream()
                .map(p -> toSummary(p, solvedIds.contains(p.getId())))
                .toList();

        return new PageResponse<>(content, page, size, result.getTotalElements(), result.getTotalPages());
    }

    public ProblemResponse findBySlug(String slug, UUID userId) {
        Problem p = problemRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new NotFoundException("Problem not found: " + slug));

        boolean solved = userId != null
                && userProgressRepository.existsByUserIdAndProblemIdAndSolvedTrue(userId, p.getId());

        return toDetail(p, solved);
    }

    private ProblemSummaryResponse toSummary(Problem p, boolean solved) {
        List<ProblemResponse.PatternSummary> patterns = p.getPatterns().stream()
                .map(pat -> new ProblemResponse.PatternSummary(pat.getId(), pat.getSlug(), pat.getName()))
                .toList();
        return new ProblemSummaryResponse(p.getId(), p.getSlug(), p.getTitle(),
                p.getDifficulty(), p.getTags(), patterns, 0.0, solved);
    }

    private ProblemResponse toDetail(Problem p, boolean solved) {
        List<ProblemResponse.PatternSummary> patterns = p.getPatterns().stream()
                .map(pat -> new ProblemResponse.PatternSummary(pat.getId(), pat.getSlug(), pat.getName()))
                .toList();

        int hintsCount = hintRepository.countByProblemId(p.getId());

        ProblemResponse.ContentDto content = problemContentRepository.findByProblemId(p.getId())
                .map(this::toContentDto)
                .orElse(null);

        List<ProblemResponse.FollowupDto> followups = problemFollowupRepository
                .findByProblemIdOrderBySortOrderAsc(p.getId()).stream()
                .map(f -> new ProblemResponse.FollowupDto(f.getQuestion(), f.getType()))
                .toList();

        return new ProblemResponse(
                p.getId(), p.getSlug(), p.getTitle(), p.getDifficulty(),
                p.getDescription(), p.getConstraints(), p.getExamples(),
                p.getTags(), patterns, solved, hintsCount, content, followups);
    }

    private ProblemResponse.ContentDto toContentDto(ProblemContent c) {
        return new ProblemResponse.ContentDto(
                c.getIntuition(), c.getBruteForce(), c.getBruteTime(), c.getBruteSpace(),
                c.getOptimalApproach(), c.getOptimalTime(), c.getOptimalSpace(),
                c.getPseudocode(), c.getJavaSolution(), c.getCommonMistakes(), c.getRecognitionNote());
    }
}
