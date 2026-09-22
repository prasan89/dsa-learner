package com.dsalearner.service;

import com.dsalearner.model.entity.*;
import com.dsalearner.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemServiceTest {
 @Mock ProblemRepository problems; @Mock UserProgressRepository progress; @Mock HintRepository hints; @Mock ProblemContentRepository content; @Mock ProblemFollowupRepository followups; @Mock UserSubscriptionRepository subscriptions;
 private ProblemService service(){return new ProblemService(problems,progress,hints,content,followups,subscriptions);}
 @Test void findAllNoFilters(){Problem p=mock(Problem.class);UUID id=UUID.randomUUID();when(p.getId()).thenReturn(id);when(p.getSlug()).thenReturn("two-sum");when(p.getTitle()).thenReturn("Two Sum");when(p.getPatterns()).thenReturn(List.of());when(p.getDifficulty()).thenReturn(com.dsalearner.model.enums.Difficulty.EASY);when(p.getTags()).thenReturn(List.of());when(problems.findAllByActiveTrueAndFreeAccessTrue(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(p)));assertEquals(1,service().findAll(null,null,0,10,null).content().size());}
 @Test void findBySlugMissing(){when(problems.findBySlugAndActiveTrue("x")).thenReturn(Optional.empty());assertThrows(com.dsalearner.exception.NotFoundException.class,()->service().findBySlug("x",null));}
 @Test void findBySlugWithContentAndFollowups(){UUID id=UUID.randomUUID();Problem p=mock(Problem.class);when(p.getId()).thenReturn(id);when(p.getSlug()).thenReturn("x");when(p.getTitle()).thenReturn("X");when(p.getPatterns()).thenReturn(List.of());when(p.getDifficulty()).thenReturn(com.dsalearner.model.enums.Difficulty.EASY);when(p.getDescription()).thenReturn("d");when(p.getConstraints()).thenReturn("c");when(p.getExamples()).thenReturn("e");when(p.getTags()).thenReturn(List.of());when(p.isFreeAccess()).thenReturn(true);when(problems.findBySlugAndActiveTrue("x")).thenReturn(Optional.of(p));when(progress.existsByUserIdAndProblemIdAndSolvedTrue(any(),eq(id))).thenReturn(true);when(hints.countByProblemId(id)).thenReturn(3);when(content.findByProblemId(id)).thenReturn(Optional.empty());when(followups.findByProblemIdOrderBySortOrderAsc(id)).thenReturn(List.of());assertTrue(service().findBySlug("x",UUID.randomUUID()).solved());assertEquals(3,service().findBySlug("x",null).hintsCount());}
 @Test void qualityCheckMissingContentDefaultsDraft(){UUID id=UUID.randomUUID();Problem p=mock(Problem.class);when(p.getId()).thenReturn(id);when(problems.findBySlugAndActiveTrue("x")).thenReturn(Optional.of(p));when(content.findByProblemId(id)).thenReturn(Optional.empty());assertEquals("DRAFT",service().getQualityCheck("x").contentStatus());}
}
