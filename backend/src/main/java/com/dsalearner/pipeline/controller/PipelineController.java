package com.dsalearner.pipeline.controller;

import com.dsalearner.pipeline.model.entity.CfAgentRun;
import com.dsalearner.pipeline.model.entity.CfLesson;
import com.dsalearner.pipeline.model.entity.CfWorkflowEvent;
import com.dsalearner.pipeline.repository.CfWorkflowEventRepository;
import com.dsalearner.pipeline.service.AgentRunService;
import com.dsalearner.pipeline.service.CostLedgerService;
import com.dsalearner.pipeline.service.PipelineService;
import com.dsalearner.pipeline.validation.ValidationResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pipeline")
@RequiredArgsConstructor
public class PipelineController {

    private final PipelineService pipelineService;
    private final AgentRunService agentRunService;
    private final CostLedgerService costLedgerService;
    private final CfWorkflowEventRepository workflowEventRepository;

    // ─── Lessons ─────────────────────────────────────────────────────────

    @GetMapping("/lessons")
    public ResponseEntity<List<CfLesson>> listLessons(
            @RequestParam(required = false) String domain) {
        if (domain != null) {
            return ResponseEntity.ok(pipelineService.getLessonsByDomain(domain));
        }
        return ResponseEntity.ok(pipelineService.getLessonsByDomain("language"));
    }

    @PostMapping("/lessons")
    public ResponseEntity<CfLesson> createLesson(
            @Valid @RequestBody CreateLessonRequest req,
            @AuthenticationPrincipal UserDetails user) {
        String actor = user != null ? "admin:" + user.getUsername() : "system";
        CfLesson lesson = pipelineService.createLesson(
                req.stableRef(), req.domainCode(), req.languageCode(),
                req.cefrLevel(), req.title(), actor);
        return ResponseEntity.status(HttpStatus.CREATED).body(lesson);
    }

    @GetMapping("/lessons/{id}")
    public ResponseEntity<CfLesson> getLesson(@PathVariable UUID id) {
        return ResponseEntity.ok(pipelineService.getLesson(id));
    }

    // ─── Workflow Triggers ────────────────────────────────────────────────

    @PostMapping("/lessons/{id}/plan")
    public ResponseEntity<CfLesson> plan(@PathVariable UUID id,
                                          @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(pipelineService.plan(id, actorFrom(user)));
    }

    @PostMapping("/lessons/{id}/generate")
    public ResponseEntity<CfLesson> generate(@PathVariable UUID id,
                                              @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(pipelineService.startGeneration(id, actorFrom(user)));
    }

    @PostMapping("/lessons/{id}/validate")
    public ResponseEntity<ValidationResult> validate(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> content,
            @AuthenticationPrincipal UserDetails user) {
        ValidationResult result = pipelineService.submitForValidation(id, content, actorFrom(user));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/lessons/{id}/approve")
    public ResponseEntity<CfLesson> approve(@PathVariable UUID id,
                                             @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(pipelineService.approve(id, actorFrom(user)));
    }

    @PostMapping("/lessons/{id}/reject")
    public ResponseEntity<CfLesson> reject(@PathVariable UUID id,
                                            @RequestBody(required = false) Map<String, String> body,
                                            @AuthenticationPrincipal UserDetails user) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(pipelineService.reject(id, actorFrom(user), notes));
    }

    // ─── Audit ────────────────────────────────────────────────────────────

    @GetMapping("/lessons/{id}/audit")
    public ResponseEntity<List<CfWorkflowEvent>> audit(@PathVariable UUID id) {
        return ResponseEntity.ok(workflowEventRepository.findByLessonIdOrderByOccurredAtDesc(id));
    }

    @GetMapping("/lessons/{id}/runs")
    public ResponseEntity<List<CfAgentRun>> runs(@PathVariable UUID id,
                                                  @RequestParam(defaultValue = "1") int version) {
        return ResponseEntity.ok(agentRunService.runsForLesson(id, version));
    }

    // ─── Cost ─────────────────────────────────────────────────────────────

    @GetMapping("/cost/daily")
    public ResponseEntity<Map<String, Object>> dailyCost() {
        BigDecimal total = costLedgerService.todayTotalCost();
        return ResponseEntity.ok(Map.of("today_usd", total));
    }

    @GetMapping("/cost/lesson/{id}")
    public ResponseEntity<Map<String, Object>> lessonCost(@PathVariable UUID id) {
        BigDecimal total = costLedgerService.totalCostForLesson(id);
        return ResponseEntity.ok(Map.of("lesson_id", id, "total_usd", total));
    }

    // ─── Health ───────────────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "content-factory"));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private String actorFrom(UserDetails user) {
        return user != null ? "admin:" + user.getUsername() : "system";
    }

    // ─── Request DTOs ─────────────────────────────────────────────────────

    record CreateLessonRequest(
            @NotBlank String stableRef,
            @NotBlank String domainCode,
            String languageCode,
            String cefrLevel,
            String title
    ) {}
}
