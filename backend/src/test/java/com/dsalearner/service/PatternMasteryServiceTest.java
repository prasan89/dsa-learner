package com.dsalearner.service;

import com.dsalearner.model.entity.*;
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
class PatternMasteryServiceTest {
 @Mock PatternMasteryRepository mastery; @Mock PatternRepository patterns; @Mock UserRepository users; @Mock UserProgressRepository progress;
 @Test void defaultsAndUpdates(){UUID pid=UUID.randomUUID(),uid=UUID.randomUUID();Pattern p=mock(Pattern.class);when(p.getId()).thenReturn(pid);when(p.getSlug()).thenReturn("arrays");when(p.getName()).thenReturn("Arrays");when(patterns.findAll(any(Sort.class))).thenReturn(List.of(p));when(mastery.findByUserId(uid)).thenReturn(List.of());PatternMasteryService s=new PatternMasteryService(mastery,patterns,users,progress);assertEquals("NOT_STARTED",s.getAllForUser(uid).get(0).status());when(patterns.findBySlug("arrays")).thenReturn(Optional.of(p));User u=mock(User.class);when(users.findById(uid)).thenReturn(Optional.of(u));when(mastery.findByUserIdAndPatternId(uid,pid)).thenReturn(Optional.empty());PatternMastery m=mock(PatternMastery.class);when(m.getMasteryScore()).thenReturn(java.math.BigDecimal.ZERO);when(m.getProblemsSolved()).thenReturn(0);when(mastery.save(any())).thenReturn(m);assertEquals("MASTERED",s.updateMastery("arrays",uid,"MASTERED").status());}
 @Test void recalculatesMastery(){UUID uid=UUID.randomUUID(),pid=UUID.randomUUID();Pattern p=mock(Pattern.class);when(p.getId()).thenReturn(pid);when(patterns.findAll()).thenReturn(List.of(p));when(users.getReferenceById(uid)).thenReturn(mock(User.class));UserProgress up=mock(UserProgress.class);when(up.isSolved()).thenReturn(true);when(up.getAttempts()).thenReturn(1);when(up.getHintsUsed()).thenReturn(0);when(progress.findByUserIdAndPatternId(uid,pid)).thenReturn(List.of(up));when(progress.countProblemsByPatternId(pid)).thenReturn(1L);when(mastery.findByUserIdAndPatternId(uid,pid)).thenReturn(Optional.empty());mastery.save(any(PatternMastery.class));new PatternMasteryService(mastery,patterns,users,progress).recalculateMasteryForUser(uid);verify(mastery).save(any(PatternMastery.class));}
}
