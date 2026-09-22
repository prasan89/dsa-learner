package com.dsalearner.service;

import com.dsalearner.model.entity.*;
import com.dsalearner.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
 @Mock UserProgressRepository progress; @Mock PatternRepository patterns; @Mock PatternMasteryRepository mastery; @Mock UserActivityRepository activity; @Mock UserSubscriptionRepository subs; @Mock ProblemRepository problems;
 @Test void emptyDashboardHasZeroStreak(){UUID uid=UUID.randomUUID();when(activity.findDatesByUserIdOrderByDateDesc(uid)).thenReturn(List.of());when(patterns.findByCategoryOrderByDisplayOrder("DSA")).thenReturn(List.of());when(patterns.findByCategoryOrderByDisplayOrder("SYSTEM_DESIGN")).thenReturn(List.of());when(mastery.findByUserId(uid)).thenReturn(List.of());when(subs.findByUserId(uid)).thenReturn(Optional.empty());DashboardResponse r=new DashboardService(progress,patterns,mastery,activity,subs,problems).getDashboard(uid);assertEquals(0,r.streak());assertEquals("FREE",r.plan());assertNull(r.nextAction());}
 @Test void streakCountsConsecutiveDays(){UUID uid=UUID.randomUUID();when(activity.findDatesByUserIdOrderByDateDesc(uid)).thenReturn(List.of(LocalDate.now(),LocalDate.now().minusDays(1),LocalDate.now().minusDays(2)));when(patterns.findByCategoryOrderByDisplayOrder("DSA")).thenReturn(List.of());when(patterns.findByCategoryOrderByDisplayOrder("SYSTEM_DESIGN")).thenReturn(List.of());when(mastery.findByUserId(uid)).thenReturn(List.of());when(subs.findByUserId(uid)).thenReturn(Optional.empty());DashboardService s=new DashboardService(progress,patterns,mastery,activity,subs,problems);assertEquals(3,s.getDashboard(uid).streak());}
 @Test void recordActivityDoesNotDuplicate(){UUID uid=UUID.randomUUID();when(activity.existsByUserIdAndActivityDate(eq(uid),any())).thenReturn(false);new DashboardService(progress,patterns,mastery,activity,subs,problems).recordActivity(uid);verify(activity).save(any(UserActivity.class));}
}
