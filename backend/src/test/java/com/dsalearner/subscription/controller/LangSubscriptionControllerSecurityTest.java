package com.dsalearner.subscription.controller;

import com.dsalearner.academy.security.CurrentUserProvider;
import com.dsalearner.subscription.dto.SubscriptionPlanDto;
import com.dsalearner.subscription.dto.SubscriptionStatusDto;
import com.dsalearner.subscription.service.EntitlementService;
import com.dsalearner.subscription.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LangSubscriptionControllerSecurityTest {

    @Mock EntitlementService entitlementService;
    @Mock SubscriptionService subscriptionService;
    @Mock CurrentUserProvider currentUserProvider;
    @Mock Authentication authentication;

    @InjectMocks LangSubscriptionController controller;

    @Test
    void getPlans_returnsActivePlans() {
        List<SubscriptionPlanDto> plans = List.of(
                new SubscriptionPlanDto("FREE", "Free", 0, "INR", 0, null),
                new SubscriptionPlanDto("PRO_MONTHLY", "Pro Monthly", 49900, "INR", 30, "langoa_pro_monthly")
        );
        when(subscriptionService.getActivePlans()).thenReturn(plans);

        var result = controller.getPlans();
        assertEquals(2, result.getBody().size());
        assertEquals("FREE", result.getBody().get(0).planCode());
    }

    @Test
    void getMySubscription_returnsSubscriptionStatus() {
        UUID userId = UUID.randomUUID();
        when(currentUserProvider.getUserId(authentication)).thenReturn(userId);
        SubscriptionStatusDto status = new SubscriptionStatusDto("FREE", false, null, null);
        when(entitlementService.getStatus(userId)).thenReturn(status);

        var result = controller.getMySubscription(authentication);
        assertEquals("FREE", result.getBody().planCode());
    }
}
