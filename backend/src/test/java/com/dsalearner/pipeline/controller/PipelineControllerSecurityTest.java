package com.dsalearner.pipeline.controller;

import com.dsalearner.config.SecurityConfig;
import com.dsalearner.pipeline.repository.CfWorkflowEventRepository;
import com.dsalearner.pipeline.service.AgentRunService;
import com.dsalearner.pipeline.service.CostLedgerService;
import com.dsalearner.pipeline.service.PipelineService;
import com.dsalearner.security.JwtService;
import com.dsalearner.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the security boundary for /api/v1/pipeline/* endpoints.
 *
 * Only /api/v1/pipeline/health is public.
 * All mutation and read endpoints require an authenticated JWT.
 *
 * Spring Security 6 + stateless session (no AuthenticationEntryPoint configured)
 * returns 403 Forbidden for unauthenticated requests rather than 401.
 * The security is correctly enforced; 403 means "not allowed" in this setup.
 *
 * The real JwtAuthFilter is active (not mocked). Its dependencies (JwtService,
 * UserDetailsServiceImpl) are mocked so no real JWT validation occurs — an
 * unauthenticated request simply has no token and is rejected by Spring Security.
 */
@WebMvcTest(PipelineController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:3000")
class PipelineControllerSecurityTest {

    @Autowired MockMvc mvc;

    @MockBean PipelineService pipelineService;
    @MockBean AgentRunService agentRunService;
    @MockBean CostLedgerService costLedgerService;
    @MockBean CfWorkflowEventRepository workflowEventRepository;

    // Mocked dependencies of JwtAuthFilter — real filter stays active.
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    // ─── Public endpoint ──────────────────────────────────────────────────

    @Test
    void healthEndpointIsPublic() throws Exception {
        mvc.perform(get("/api/v1/pipeline/health"))
                .andExpect(status().isOk());
    }

    // ─── Protected read endpoints ─────────────────────────────────────────

    @Test
    void listLessonsRequiresAuth() throws Exception {
        mvc.perform(get("/api/v1/pipeline/lessons"))
                .andExpect(status().isForbidden()); // 403 — no AuthenticationEntryPoint configured
    }

    @Test
    void getLessonRequiresAuth() throws Exception {
        mvc.perform(get("/api/v1/pipeline/lessons/00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isForbidden());
    }

    @Test
    void auditRequiresAuth() throws Exception {
        mvc.perform(get("/api/v1/pipeline/lessons/00000000-0000-0000-0000-000000000001/audit"))
                .andExpect(status().isForbidden());
    }

    // ─── Protected mutation endpoints ─────────────────────────────────────

    @Test
    void createLessonRequiresAuth() throws Exception {
        mvc.perform(post("/api/v1/pipeline/lessons")
                        .contentType("application/json")
                        .content("{\"stableRef\":\"x\",\"domainCode\":\"language\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void approveRequiresAuth() throws Exception {
        mvc.perform(post("/api/v1/pipeline/lessons/00000000-0000-0000-0000-000000000001/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectRequiresAuth() throws Exception {
        mvc.perform(post("/api/v1/pipeline/lessons/00000000-0000-0000-0000-000000000001/reject"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validateRequiresAuth() throws Exception {
        mvc.perform(post("/api/v1/pipeline/lessons/00000000-0000-0000-0000-000000000001/validate")
                        .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void dailyCostRequiresAuth() throws Exception {
        mvc.perform(get("/api/v1/pipeline/cost/daily"))
                .andExpect(status().isForbidden());
    }
}
