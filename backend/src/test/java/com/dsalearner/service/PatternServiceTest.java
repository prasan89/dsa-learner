package com.dsalearner.service;

import com.dsalearner.model.entity.Pattern;
import com.dsalearner.model.entity.PatternMastery;
import com.dsalearner.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatternServiceTest {
 @Mock PatternRepository patterns; @Mock PatternMasteryRepository masteries;
 private Pattern p(String slug,String clues){Pattern p=mock(Pattern.class);when(p.getId()).thenReturn(UUID.randomUUID());when(p.getSlug()).thenReturn(slug);when(p.getName()).thenReturn(slug);when(p.getRecognitionClues()).thenReturn(clues);when(p.getSummary()).thenReturn("summary");when(p.getDisplayOrder()).thenReturn(1);when(p.getTemplateCode()).thenReturn("code");when(p.getLessonMarkdown()).thenReturn("lesson");when(p.getTimeComplexity()).thenReturn("O(n)");when(p.getSpaceComplexity()).thenReturn("O(1)");when(p.getCategory()).thenReturn("DSA");return p;}
 @Test void findAllAndCategory(){Pattern p=p("arrays","a\nb");when(patterns.findAll(any(Sort.class))).thenReturn(List.of(p));when(patterns.findByCategoryOrderByDisplayOrder("DSA")).thenReturn(List.of(p));PatternService s=new PatternService(patterns,masteries);assertEquals(1,s.findAll().size());assertEquals(2,s.findAll().get(0).recognitionClues().size());assertEquals(1,s.findAllByCategory("DSA").size());}
 @Test void userDefaultsToNotStarted(){Pattern p=p("arrays","a");when(patterns.findAll(any(Sort.class))).thenReturn(List.of(p));when(masteries.findByUserId(any())).thenReturn(List.of());assertEquals("NOT_STARTED",new PatternService(patterns,masteries).findAllForUser(UUID.randomUUID()).get(0).masteryStatus());}
 @Test void missingSlugThrows(){when(patterns.findBySlug("x")).thenReturn(Optional.empty());assertThrows(com.dsalearner.exception.NotFoundException.class,()->new PatternService(patterns,masteries).findBySlug("x"));}
}
