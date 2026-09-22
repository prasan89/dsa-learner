package com.dsalearner.service;

import com.dsalearner.model.entity.*;
import com.dsalearner.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HintServiceTest {
 @Mock HintRepository hints; @Mock UserHintRepository userHints; @Mock ProblemRepository problems; @Mock UserRepository users; @Mock UserProgressRepository progress;
 @Test void getHintsReturnsUnlockedState(){UUID pid=UUID.randomUUID(),hid=UUID.randomUUID(),uid=UUID.randomUUID();Problem p=mock(Problem.class);Hint h=mock(Hint.class);when(p.getId()).thenReturn(pid);when(h.getId()).thenReturn(hid);when(h.getLevel()).thenReturn(1);when(h.getLabel()).thenReturn("Concept");when(h.getContent()).thenReturn("c");when(problems.findBySlug("x")).thenReturn(Optional.of(p));when(hints.findByProblemIdOrderByLevel(pid)).thenReturn(List.of(h));when(userHints.findUnlockedHintIdsByUserId(uid)).thenReturn(List.of(hid));assertTrue(new HintService(hints,userHints,problems,users,progress).getHints("x",uid).get(0).unlocked());}
 @Test void unlockSavesAndUpdatesProgress(){UUID pid=UUID.randomUUID(),hid=UUID.randomUUID(),uid=UUID.randomUUID();Problem p=mock(Problem.class);Hint h=mock(Hint.class);User u=mock(User.class);UserProgress up=mock(UserProgress.class);when(p.getId()).thenReturn(pid);when(h.getId()).thenReturn(hid);when(h.getLevel()).thenReturn(1);when(h.getLabel()).thenReturn("Concept");when(h.getContent()).thenReturn("c");when(problems.findBySlug("x")).thenReturn(Optional.of(p));when(hints.findByProblemIdAndLevel(pid,1)).thenReturn(Optional.of(h));when(userHints.existsByUserIdAndHintId(uid,hid)).thenReturn(false);when(users.findById(uid)).thenReturn(Optional.of(u));when(progress.findByUserIdAndProblemId(uid,pid)).thenReturn(Optional.of(up));when(up.getHintsUsed()).thenReturn(2);assertTrue(new HintService(hints,userHints,problems,users,progress).unlockHint("x",1,uid).unlocked());verify(up).setHintsUsed(3);verify(userHints).save(any());}
 @Test void missingHintThrows(){UUID pid=UUID.randomUUID();Problem p=mock(Problem.class);when(p.getId()).thenReturn(pid);when(problems.findBySlug("x")).thenReturn(Optional.of(p));when(hints.findByProblemIdAndLevel(pid,9)).thenReturn(Optional.empty());assertThrows(com.dsalearner.exception.NotFoundException.class,()->new HintService(hints,userHints,problems,users,progress).unlockHint("x",9,UUID.randomUUID()));}
}
