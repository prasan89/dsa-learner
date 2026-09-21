package com.dsalearner.service;

import com.dsalearner.dto.response.HintResponse;
import com.dsalearner.exception.NotFoundException;
import com.dsalearner.model.entity.*;
import com.dsalearner.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HintService {

    private final HintRepository hintRepository;
    private final UserHintRepository userHintRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;

    public List<HintResponse> getHints(String problemSlug, UUID userId) {
        Problem problem = problemRepository.findBySlug(problemSlug)
                .orElseThrow(() -> new NotFoundException("Problem not found: " + problemSlug));

        List<Hint> hints = hintRepository.findByProblemIdOrderByLevel(problem.getId());
        Set<UUID> unlockedIds = Set.copyOf(userHintRepository.findUnlockedHintIdsByUserId(userId));

        return hints.stream()
                .map(h -> new HintResponse(h.getId(), h.getLevel(), h.getContent(), unlockedIds.contains(h.getId())))
                .toList();
    }

    @Transactional
    public HintResponse unlockHint(String problemSlug, int level, UUID userId) {
        Problem problem = problemRepository.findBySlug(problemSlug)
                .orElseThrow(() -> new NotFoundException("Problem not found: " + problemSlug));

        Hint hint = hintRepository.findByProblemIdAndLevel(problem.getId(), level)
                .orElseThrow(() -> new NotFoundException("Hint level " + level + " not found"));

        if (!userHintRepository.existsByUserIdAndHintId(userId, hint.getId())) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));
            userHintRepository.save(UserHint.builder().user(user).hint(hint).build());

            // Track hints used in user_progress
            userProgressRepository.findByUserIdAndProblemId(userId, problem.getId())
                    .ifPresent(up -> {
                        up.setHintsUsed(up.getHintsUsed() + 1);
                        userProgressRepository.save(up);
                    });
        }

        return new HintResponse(hint.getId(), hint.getLevel(), hint.getContent(), true);
    }
}
