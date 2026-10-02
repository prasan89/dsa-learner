package com.dsalearner.security;

import com.dsalearner.academy.controller.AcademyController;
import com.dsalearner.academy.security.CurrentUserProvider;
import com.dsalearner.academy.service.AcademyService;
import com.dsalearner.civilization.service.LearningRewardService;
import com.dsalearner.config.SecurityConfig;
import com.dsalearner.controller.ProblemController;
import com.dsalearner.economy.antiabuse.EconomyRateLimiter;
import com.dsalearner.economy.antiabuse.SuspiciousActivityService;
import com.dsalearner.service.ProblemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies cross-domain access enforcement:
 * - DSA user cannot reach Language endpoints
 * - Language user cannot reach DSA endpoints
 * - Unauthenticated requests are rejected
 * - Correct domain gets through to the service layer
 */
@WebMvcTest(controllers = {AcademyController.class, ProblemController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:3000")
class DomainIsolationTest {

    @Autowired MockMvc mvc;

    @MockBean AcademyService academyService;
    @MockBean LearningRewardService learningRewardService;
    @MockBean CurrentUserProvider currentUserProvider;
    @MockBean ProblemService problemService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;
    @MockBean DomainAuthorizationService domainAuthService;
    @MockBean EconomyRateLimiter economyRateLimiter;
    @MockBean SuspiciousActivityService suspiciousActivityService;

    // ── Unauthenticated ──────────────────────────────────────────────────────

    @Test
    void unauthenticated_languageEndpoint_returns403() throws Exception {
        mvc.perform(get("/api/v1/academy/german/curriculum"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticated_dsaEndpoint_returns403() throws Exception {
        mvc.perform(get("/api/problems"))
                .andExpect(status().isForbidden());
    }

    // ── DSA user hits Language endpoint ──────────────────────────────────────

    @Test
    @WithMockUser(authorities = {"ROLE_USER", "ROLE_DOMAIN_DSA"})
    void dsaUser_languageEndpoint_returns403() throws Exception {
        doThrow(new AccessDeniedException("not enrolled in language domain"))
                .when(domainAuthService).requireDomain(any(), eq("language"));

        mvc.perform(get("/api/v1/academy/german/curriculum"))
                .andExpect(status().isForbidden());
    }

    // ── Language user hits DSA endpoint ──────────────────────────────────────

    @Test
    @WithMockUser(authorities = {"ROLE_USER", "ROLE_DOMAIN_LANGUAGE"})
    void languageUser_dsaEndpoint_returns403() throws Exception {
        doThrow(new AccessDeniedException("not enrolled in dsa domain"))
                .when(domainAuthService).requireDomain(any(), eq("dsa"));

        mvc.perform(get("/api/problems"))
                .andExpect(status().isForbidden());
    }

    // ── Correct domain passes through ────────────────────────────────────────

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001", authorities = {"ROLE_USER", "ROLE_DOMAIN_LANGUAGE"})
    void languageUser_languageEndpoint_reaches_service() throws Exception {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        org.mockito.Mockito.when(currentUserProvider.getUserId(any())).thenReturn(userId);
        org.mockito.Mockito.when(academyService.getCurriculum(eq("german"), eq(userId)))
                .thenReturn(null);

        mvc.perform(get("/api/v1/academy/german/curriculum"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000002", authorities = {"ROLE_USER", "ROLE_DOMAIN_DSA"})
    void dsaUser_dsaEndpoint_reaches_service() throws Exception {
        org.mockito.Mockito.when(problemService.findAll(any(), any(), any(int.class), any(int.class), any()))
                .thenReturn(new com.dsalearner.dto.response.PageResponse<>(java.util.List.of(), 0, 20, 0L, 0));

        mvc.perform(get("/api/problems"))
                .andExpect(status().isOk());
    }
}
