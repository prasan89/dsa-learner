package com.dsalearner.service;

import com.dsalearner.dto.response.PageResponse;
import com.dsalearner.dto.response.ProblemResponse;
import com.dsalearner.dto.response.ProblemSummaryResponse;
import com.dsalearner.dto.response.QualityCheckResponse;
import com.dsalearner.exception.NotFoundException;
import com.dsalearner.model.entity.Problem;
import com.dsalearner.model.entity.ProblemContent;
import com.dsalearner.model.entity.UserSubscription;
import com.dsalearner.model.enums.Difficulty;
import com.dsalearner.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository          problemRepository;
    private final UserProgressRepository     userProgressRepository;
    private final HintRepository             hintRepository;
    private final ProblemContentRepository   problemContentRepository;
    private final ProblemFollowupRepository  problemFollowupRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    public PageResponse<ProblemSummaryResponse> findAll(String difficulty, String patternId,
                                                         int page, int size, UUID userId) {
        boolean pro = isPro(userId);
        PageRequest pageable = PageRequest.of(page, size, Sort.by("title"));
        Page<Problem> result;

        if (difficulty != null && patternId != null) {
            result = pro
                ? problemRepository.findAllByActiveTrueAndPatternIdAndDifficulty(
                        UUID.fromString(patternId), Difficulty.valueOf(difficulty.toUpperCase()), pageable)
                : problemRepository.findAllByActiveTrueAndFreeAccessTrueAndPatternIdAndDifficulty(
                        UUID.fromString(patternId), Difficulty.valueOf(difficulty.toUpperCase()), pageable);
        } else if (patternId != null) {
            result = pro
                ? problemRepository.findAllByActiveTrueAndPatternId(UUID.fromString(patternId), pageable)
                : problemRepository.findAllByActiveTrueAndFreeAccessTrueAndPatternId(UUID.fromString(patternId), pageable);
        } else if (difficulty != null) {
            result = pro
                ? problemRepository.findAllByActiveTrueAndDifficulty(Difficulty.valueOf(difficulty.toUpperCase()), pageable)
                : problemRepository.findAllByActiveTrueAndFreeAccessTrueAndDifficulty(Difficulty.valueOf(difficulty.toUpperCase()), pageable);
        } else {
            result = pro
                ? problemRepository.findAllByActiveTrue(pageable)
                : problemRepository.findAllByActiveTrueAndFreeAccessTrue(pageable);
        }

        Set<UUID> solvedIds = userId == null ? Set.of()
                : userProgressRepository.findSolvedProblemIdsByUserId(userId);

        List<ProblemSummaryResponse> content = result.getContent().stream()
                .map(p -> toSummary(p, solvedIds.contains(p.getId()), false))
                .toList();

        return new PageResponse<>(content, page, size, result.getTotalElements(), result.getTotalPages());
    }

    public ProblemResponse findBySlug(String slug, UUID userId) {
        Problem p = problemRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new NotFoundException("Problem not found: " + slug));

        if (!p.isFreeAccess() && !isPro(userId)) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Pro subscription required");
        }

        boolean solved = userId != null
                && userProgressRepository.existsByUserIdAndProblemIdAndSolvedTrue(userId, p.getId());

        return toDetail(p, solved);
    }

    public boolean isPro(UUID userId) {
        if (userId == null) return false;
        return userSubscriptionRepository.findByUserId(userId)
                .map(UserSubscription::isPro)
                .orElse(false);
    }

    private ProblemSummaryResponse toSummary(Problem p, boolean solved, boolean locked) {
        List<ProblemResponse.PatternSummary> patterns = p.getPatterns().stream()
                .map(pat -> new ProblemResponse.PatternSummary(pat.getId(), pat.getSlug(), pat.getName()))
                .toList();
        return new ProblemSummaryResponse(p.getId(), p.getSlug(), p.getTitle(),
                p.getDifficulty(), p.getTags(), patterns, 0.0, solved, locked);
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

    public QualityCheckResponse getQualityCheck(String slug) {
        Problem p = problemRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new NotFoundException("Problem not found: " + slug));

        return problemContentRepository.findByProblemId(p.getId())
                .map(c -> QualityCheckResponse.from(slug, c.getContentStatus(), c.getQualityFlags()))
                .orElse(QualityCheckResponse.from(slug, "DRAFT", null));
    }

    private ProblemResponse.ContentDto toContentDto(ProblemContent c) {
        return new ProblemResponse.ContentDto(
                c.getIntuition(),
                c.getGuidedReasoning(),
                c.getSolution(),
                c.getRecognitionNote(),
                c.getPatternRecognitionClues(),
                c.getWhenToUse(),
                c.getWhenNotToUse(),
                c.getBruteForce(),
                c.getBruteTime(),
                c.getBruteSpace(),
                c.getOptimalApproach(),
                c.getOptimalTime(),
                c.getOptimalSpace(),
                c.getPseudocode(),
                c.getWhyThisWorks(),
                c.getInvariant(),
                c.getCommonMistakes(),
                c.getSeniorVariations());
    }
}
