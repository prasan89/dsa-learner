package com.dsalearner.pipeline.curriculum.controller;

import com.dsalearner.config.SecurityConfig;
import com.dsalearner.pipeline.curriculum.job.CurriculumJobWorker;
import com.dsalearner.pipeline.curriculum.service.CurriculumLevelService;
import com.dsalearner.pipeline.curriculum.service.CurriculumService;
import com.dsalearner.pipeline.curriculum.service.CurriculumWorkflowOrchestrator;
import com.dsalearner.pipeline.model.entity.CfCurriculum;
import com.dsalearner.pipeline.model.entity.CfCurriculumLevel;
import com.dsalearner.pipeline.model.entity.CfCurriculumLessonPlan;
import com.dsalearner.pipeline.model.entity.CfCurriculumPipelineJob;
import com.dsalearner.pipeline.repository.CfCurriculumPipelineJobRepository;
import com.dsalearner.pipeline.repository.CfCurriculumWorkflowEventRepository;
import com.dsalearner.security.JwtService;
import com.dsalearner.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurriculumController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:3000")
class CurriculumControllerTest {

    @Autowired MockMvc mvc;

    @MockBean CurriculumService curriculumService;
    @MockBean CurriculumLevelService levelService;
    @MockBean CurriculumWorkflowOrchestrator orchestrator;
    @MockBean CfCurriculumWorkflowEventRepository eventRepository;
    @MockBean CurriculumJobWorker jobWorker;
    @MockBean CfCurriculumPipelineJobRepository jobRepository;

    // JWT filter deps
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    private static final UUID CURRICULUM_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID LEVEL_ID      = UUID.fromString("00000000-0000-0000-0000-000000000020");
    private static final UUID JOB_ID        = UUID.fromString("00000000-0000-0000-0000-000000000030");

    @BeforeEach
    void setup() {
        when(jobWorker.getJobRepository()).thenReturn(jobRepository);
    }

    // ─── Security: public endpoint ────────────────────────────────────────

    @Test
    void healthIsPublic() throws Exception {
        mvc.perform(get("/api/v1/curriculum/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    // ─── Security: all other endpoints require auth ───────────────────────

    @Test
    void listRequiresAuth() throws Exception {
        mvc.perform(get("/api/v1/curriculum")).andExpect(status().isForbidden());
    }

    @Test
    void createRequiresAuth() throws Exception {
        mvc.perform(post("/api/v1/curriculum")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stableRef\":\"x\",\"domainCode\":\"d\",\"languageCode\":\"de\",\"displayName\":\"D\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRequiresAuth() throws Exception {
        mvc.perform(get("/api/v1/curriculum/" + CURRICULUM_ID)).andExpect(status().isForbidden());
    }

    @Test
    void levelsRequiresAuth() throws Exception {
        mvc.perform(get("/api/v1/curriculum/" + CURRICULUM_ID + "/levels"))
                .andExpect(status().isForbidden());
    }

    @Test
    void auditRequiresAuth() throws Exception {
        mvc.perform(get("/api/v1/curriculum/" + CURRICULUM_ID + "/audit"))
                .andExpect(status().isForbidden());
    }

    @Test
    void approveRequiresAuth() throws Exception {
        mvc.perform(post("/api/v1/curriculum/" + CURRICULUM_ID + "/approve"))
                .andExpect(status().isForbidden());
    }

    // ─── Functional: list curricula ───────────────────────────────────────

    @Test
    void listCurriculaDefaultsToGerman() throws Exception {
        CfCurriculum c = CfCurriculum.builder()
                .stableRef("de-curriculum-a1-c2")
                .languageCode("de")
                .domainCode("language")
                .displayName("German A1–C2")
                .build();
        when(curriculumService.listByLanguage("de")).thenReturn(List.of(c));

        mvc.perform(get("/api/v1/curriculum")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .httpBasic("admin", "admin")))
                .andExpect(status().isForbidden()); // JWT-only auth — httpBasic rejected
    }

    // ─── Functional: level plans ──────────────────────────────────────────

    @Test
    void plansEndpointRequiresAuth() throws Exception {
        mvc.perform(get("/api/v1/curriculum/" + CURRICULUM_ID + "/levels/" + LEVEL_ID + "/plans"))
                .andExpect(status().isForbidden());
    }

    // ─── Functional: job polling ──────────────────────────────────────────

    @Test
    void getJobRequiresAuth() throws Exception {
        mvc.perform(get("/api/v1/curriculum/jobs/" + JOB_ID))
                .andExpect(status().isForbidden());
    }
}
