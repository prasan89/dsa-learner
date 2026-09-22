package com.dsalearner.service;

import com.dsalearner.model.entity.*;
import com.dsalearner.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpacedRepetitionServiceTest {
 @Mock SpacedRepetitionRepository repo; @Mock UserRepository users; @Mock ProblemRepository problems;
 @Test void schedulesFirstReview(){UUID u=UUID.randomUUID(),p=UUID.randomUUID();when(users.getReferenceById(u)).thenReturn(mock(User.class));when(problems.getReferenceById(p)).thenReturn(mock(Problem.class));when(repo.findTopByUserIdAndProblemIdOrderByCreatedAtDesc(u,p)).thenReturn(Optional.empty());new SpacedRepetitionService(repo,users,problems).scheduleAfterSolve(u,p);verify(repo).save(any(SpacedRepetitionReview.class));}
 @Test void doesNotDuplicateExisting(){UUID u=UUID.randomUUID(),p=UUID.randomUUID();when(repo.findTopByUserIdAndProblemIdOrderByCreatedAtDesc(u,p)).thenReturn(Optional.of(mock(SpacedRepetitionReview.class)));new SpacedRepetitionService(repo,users,problems).scheduleAfterSolve(u,p);verify(repo,never()).save(any());}
 @Test void countToday(){UUID u=UUID.randomUUID();when(repo.countByUserIdAndDueDateAndCompletedAtIsNull(eq(u),any())).thenReturn(3L);assertEquals(3,new SpacedRepetitionService(repo,users,problems).countTodayDue(u));}
 @Test void completesGoodAndBadReviews(){UUID u=UUID.randomUUID(),p=UUID.randomUUID();SpacedRepetitionReview r=mock(SpacedRepetitionReview.class);when(r.getRepetition()).thenReturn(0);when(r.getEaseFactor()).thenReturn(new java.math.BigDecimal("2.5"));when(r.getUser()).thenReturn(mock(User.class));when(r.getProblem()).thenReturn(mock(Problem.class));when(repo.findTopByUserIdAndProblemIdOrderByCreatedAtDesc(u,p)).thenReturn(Optional.of(r));SpacedRepetitionService s=new SpacedRepetitionService(repo,users,problems);s.completeReview(u,p,5);s.completeReview(u,p,2);verify(repo,atLeast(2)).save(any(SpacedRepetitionReview.class));}
}
