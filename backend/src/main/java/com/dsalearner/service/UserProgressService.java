package com.dsalearner.service;

import com.dsalearner.dto.response.UserProgressResponse;
import com.dsalearner.model.enums.Difficulty;
import com.dsalearner.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProgressService {

    private final UserProgressRepository userProgressRepository;

    public UserProgressResponse getProgress(UUID userId) {
        long totalSolved  = userProgressRepository.countByUserIdAndSolvedTrue(userId);
        long easySolved   = userProgressRepository.countSolvedByUserIdAndDifficulty(userId, Difficulty.EASY);
        long mediumSolved = userProgressRepository.countSolvedByUserIdAndDifficulty(userId, Difficulty.MEDIUM);
        long hardSolved   = userProgressRepository.countSolvedByUserIdAndDifficulty(userId, Difficulty.HARD);

        return new UserProgressResponse(totalSolved, easySolved, mediumSolved, hardSolved);
    }
}
