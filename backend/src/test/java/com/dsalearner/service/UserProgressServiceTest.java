package com.dsalearner.service;

import com.dsalearner.model.enums.Difficulty;
import com.dsalearner.repository.UserProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProgressServiceTest {
 @Mock UserProgressRepository repo;
 @Test void returnsDifficultyBreakdown(){UUID id=UUID.randomUUID();when(repo.countByUserIdAndSolvedTrue(id)).thenReturn(10L);when(repo.countSolvedByUserIdAndDifficulty(id,Difficulty.EASY)).thenReturn(5L);when(repo.countSolvedByUserIdAndDifficulty(id,Difficulty.MEDIUM)).thenReturn(3L);when(repo.countSolvedByUserIdAndDifficulty(id,Difficulty.HARD)).thenReturn(2L);var r=new UserProgressService(repo).getProgress(id);assertEquals(10,r.totalSolved());assertEquals(5,r.easySolved());assertEquals(3,r.mediumSolved());assertEquals(2,r.hardSolved());}
}
