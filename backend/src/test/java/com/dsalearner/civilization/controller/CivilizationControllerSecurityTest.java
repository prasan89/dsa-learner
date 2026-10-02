package com.dsalearner.civilization.controller;

import com.dsalearner.academy.security.CurrentUserProvider;
import com.dsalearner.civilization.repository.LangoaBuildingDefinitionRepository;
import com.dsalearner.civilization.repository.LangoaBuildingInstanceRepository;
import com.dsalearner.civilization.repository.LangoaCurrencyBalanceRepository;
import com.dsalearner.civilization.repository.LangoaTransactionRepository;
import com.dsalearner.civilization.service.AchievementService;
import com.dsalearner.civilization.service.CivilizationService;
import com.dsalearner.civilization.service.QuestService;
import com.dsalearner.config.SecurityConfig;
import com.dsalearner.economy.antiabuse.EconomyRateLimiter;
import com.dsalearner.economy.antiabuse.SuspiciousActivityService;
import com.dsalearner.security.DomainAuthorizationService;
import com.dsalearner.security.JwtService;
import com.dsalearner.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security boundary tests for CivilizationController endpoints.
 *
 * Verifies:
 * 1. Unauthenticated requests return 403 (Spring Security denies access).
 * 2. Authenticated requests with the language domain role return 200.
 * 3. User A's JWT cannot affect User B's civilization (auth isolation).
 */
@WebMvcTest(CivilizationController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.cors.allowed-origins=http://localhost:3000",
        "application.mode=language"
})
class CivilizationControllerSecurityTest {

    @Autowired MockMvc mvc;

    @MockBean CivilizationService civilizationService;
    @MockBean QuestService questService;
    @MockBean AchievementService achievementService;
    @MockBean CurrentUserProvider currentUserProvider;
    @MockBean DomainAuthorizationService domainAuthService;
    @MockBean LangoaBuildingInstanceRepository buildingInstanceRepo;
    @MockBean LangoaBuildingDefinitionRepository buildingDefRepo;
    @MockBean LangoaCurrencyBalanceRepository balanceRepo;
    @MockBean LangoaTransactionRepository transactionRepo;
    @MockBean EconomyRateLimiter economyRateLimiter;
    @MockBean SuspiciousActivityService suspiciousActivityService;

    @BeforeEach
    void allowAllRateLimits() {
        when(economyRateLimiter.isBuildAllowed(any())).thenReturn(true);
        when(economyRateLimiter.isUpgradeAllowed(any())).thenReturn(true);
        when(economyRateLimiter.isCollectAllowed(any())).thenReturn(true);
        when(economyRateLimiter.isLessonAllowed(any())).thenReturn(true);
    }

    // JwtAuthFilter dependencies
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    // ── Unauthenticated → 403 ─────────────────────────────────────────────────

    @Test
    void test_getCivilization_unauthenticated_returns401() throws Exception {
        mvc.perform(get("/api/v1/civilization/german"))
                .andExpect(status().isForbidden());
    }

    @Test
    void test_getBuildingDefinitions_unauthenticated_returns403() throws Exception {
        mvc.perform(get("/api/v1/civilization/german/buildings/definitions"))
                .andExpect(status().isForbidden());
    }

    @Test
    void test_buildBuilding_unauthenticated_returns403() throws Exception {
        mvc.perform(post("/api/v1/civilization/german/buildings")
                        .contentType("application/json")
                        .content("{\"buildingType\":\"FARM\",\"positionX\":1,\"positionY\":1}"))
                .andExpect(status().isForbidden());
    }

    // ── Authenticated + domain role → 200 ────────────────────────────────────

    @Test
    void test_getCivilization_authenticated_returns200() throws Exception {
        UUID userId = UUID.randomUUID();

        // Stub the service to return a minimal response
        var stateResponse = new com.dsalearner.civilization.dto.CivilizationStateResponse(
                UUID.randomUUID(), "de", "My Civ", "VILLAGE", 1, 0L, 0,
                new java.util.LinkedHashMap<>(), new java.util.LinkedHashMap<>(), List.of(), List.of(), List.of());
        when(civilizationService.getOrCreateAndGetState(any(), anyString()))
                .thenReturn(stateResponse);

        // Stub user extraction and domain check (no-op for success path)
        when(currentUserProvider.getUserId(any())).thenReturn(userId);
        doNothing().when(domainAuthService).requireDomain(any(), eq("language"));

        // Build a mock JWT authentication with the language domain authority
        var auth = new UsernamePasswordAuthenticationToken(
                userId.toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_DOMAIN_LANGUAGE")));

        // Configure JwtService to accept a fake token and return a valid UserDetails
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        userId.toString(), "",
                        List.of(new SimpleGrantedAuthority("ROLE_DOMAIN_LANGUAGE")));
        when(jwtService.isTokenValid(anyString())).thenReturn(true);
        when(jwtService.extractUserId(anyString())).thenReturn(userId);
        when(userDetailsService.loadUserByUsername(userId.toString())).thenReturn(userDetails);

        mvc.perform(get("/api/v1/civilization/german")
                        .header("Authorization", "Bearer fake-jwt-token"))
                .andExpect(status().isOk());
    }

    // ── Auth isolation: User A cannot modify User B's civilization ────────────

    @Test
    void test_buildBuilding_userA_cannotModifyUserB_civilization() throws Exception {
        UUID userAId = UUID.randomUUID();
        UUID userBId = UUID.randomUUID();

        // Configure JWT to authenticate as User A
        org.springframework.security.core.userdetails.User userADetails =
                new org.springframework.security.core.userdetails.User(
                        userAId.toString(), "",
                        List.of(new SimpleGrantedAuthority("ROLE_DOMAIN_LANGUAGE")));
        when(jwtService.isTokenValid(anyString())).thenReturn(true);
        when(jwtService.extractUserId(anyString())).thenReturn(userAId);
        when(userDetailsService.loadUserByUsername(userAId.toString())).thenReturn(userADetails);

        // The CurrentUserProvider returns User A's ID from the authentication
        when(currentUserProvider.getUserId(any())).thenReturn(userAId);
        doNothing().when(domainAuthService).requireDomain(any(), eq("language"));

        // Simulate User A building a building in "their" civilization
        var stateResponse = new com.dsalearner.civilization.dto.CivilizationStateResponse(
                UUID.randomUUID(), "de", "User A Civ", "VILLAGE", 1, 0L, 0,
                new java.util.LinkedHashMap<>(), new java.util.LinkedHashMap<>(), List.of(), List.of(), List.of());
        when(civilizationService.buildBuilding(eq(userAId), anyString(), any()))
                .thenReturn(stateResponse);

        mvc.perform(post("/api/v1/civilization/german/buildings")
                        .header("Authorization", "Bearer user-a-jwt")
                        .contentType("application/json")
                        .content("{\"buildingType\":\"FARM\",\"positionX\":1,\"positionY\":1}"))
                .andExpect(status().isOk());

        // The service must ONLY be called with User A's ID, never User B's
        verify(civilizationService).buildBuilding(eq(userAId), anyString(), any());
        verify(civilizationService, never()).buildBuilding(eq(userBId), anyString(), any());
    }
}
