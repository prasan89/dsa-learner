package com.dsalearner.service;

import com.dsalearner.dto.response.PatternMasteryResponse;
import com.dsalearner.exception.NotFoundException;
import com.dsalearner.model.entity.*;
import com.dsalearner.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatternMasteryService {

    private final PatternMasteryRepository masteryRepository;
    private final PatternRepository patternRepository;
    private final UserRepository userRepository;

    public List<PatternMasteryResponse> getAllForUser(UUID userId) {
        List<Pattern> allPatterns = patternRepository.findAll(
                org.springframework.data.domain.Sort.by("displayOrder"));
        List<PatternMastery> existing = masteryRepository.findByUserId(userId);

        return allPatterns.stream().map(p -> {
            String status = existing.stream()
                    .filter(m -> m.getPattern().getId().equals(p.getId()))
                    .findFirst()
                    .map(m -> m.getStatus().name())
                    .orElse("NOT_STARTED");
            return new PatternMasteryResponse(p.getId(), p.getSlug(), p.getName(), status);
        }).toList();
    }

    @Transactional
    public PatternMasteryResponse updateMastery(String patternSlug, UUID userId, String status) {
        Pattern pattern = patternRepository.findBySlug(patternSlug)
                .orElseThrow(() -> new NotFoundException("Pattern not found: " + patternSlug));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        PatternMastery.MasteryStatus masteryStatus = PatternMastery.MasteryStatus.valueOf(status);

        PatternMastery mastery = masteryRepository
                .findByUserIdAndPatternId(userId, pattern.getId())
                .orElse(PatternMastery.builder().user(user).pattern(pattern).build());

        mastery.setStatus(masteryStatus);
        mastery.setUpdatedAt(Instant.now());
        masteryRepository.save(mastery);

        return new PatternMasteryResponse(pattern.getId(), pattern.getSlug(), pattern.getName(), status);
    }
}
