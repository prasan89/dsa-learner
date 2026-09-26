package com.dsalearner.academy.controller;

import com.dsalearner.academy.security.CurrentUserProvider;
import com.dsalearner.academy.service.AcademyService;
import com.dsalearner.config.SecurityConfig;
import com.dsalearner.security.JwtService;
import com.dsalearner.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security boundary tests for Academy endpoints.
 * All Academy endpoints require authentication — unauthenticated requests get 403.
 */
@WebMvcTest(AcademyController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:3000")
class AcademyControllerSecurityTest {

    @Autowired MockMvc mvc;

    @MockBean AcademyService academyService;
    @MockBean CurrentUserProvider currentUserProvider;

    // JwtAuthFilter dependencies
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    @Test
    void getCurriculumRequiresAuth() throws Exception {
        mvc.perform(get("/api/v1/academy/de/curriculum"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getLessonRequiresAuth() throws Exception {
        mvc.perform(get("/api/v1/academy/de/lessons/00000000-0000-0000-0000-000000000001"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStepRequiresAuth() throws Exception {
        mvc.perform(patch("/api/v1/academy/de/lessons/00000000-0000-0000-0000-000000000001/step")
                        .contentType("application/json")
                        .content("{\"stepIndex\":2}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void completeLessonRequiresAuth() throws Exception {
        mvc.perform(post("/api/v1/academy/de/lessons/00000000-0000-0000-0000-000000000001/complete")
                        .contentType("application/json")
                        .content("{\"score\":85}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProgressRequiresAuth() throws Exception {
        mvc.perform(get("/api/v1/academy/de/progress"))
                .andExpect(status().isForbidden());
    }
}
