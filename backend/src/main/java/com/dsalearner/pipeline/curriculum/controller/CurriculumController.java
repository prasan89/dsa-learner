package com.dsalearner.pipeline.curriculum.controller;

import com.dsalearner.pipeline.curriculum.job.CurriculumJobWorker;
import com.dsalearner.pipeline.curriculum.service.CurriculumLevelService;
import com.dsalearner.pipeline.curriculum.service.CurriculumService;
import com.dsalearner.pipeline.curriculum.service.CurriculumWorkflowOrchestrator;
import com.dsalearner.pipeline.model.entity.CfCurriculum;
import com.dsalearner.pipeline.model.entity.CfCurriculumLevel;
import com.dsalearner.pipeline.model.entity.CfCurriculumLessonPlan;
import com.dsalearner.pipeline.model.entity.CfCurriculumPipelineJob;
import com.dsalearner.pipeline.model.entity.CfCurriculumWorkflowEvent;
import com.dsalearner.pipeline.repository.CfCurriculumWorkflowEventRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/curriculum")
@RequiredArgsConstructor
public class CurriculumController {

    private final CurriculumService curriculumService;
    private final CurriculumLevelService levelService;
    private final CurriculumWorkflowOrchestrator orchestrator;
    private final CfCurriculumWorkflowEventRepository eventRepository;
    private final CurriculumJobWorker jobWorker;

    // ─── Curriculum CRUD ──────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<CfCurriculum>> list(
            @RequestParam(defaultValue = "de") String languageCode) {
        return ResponseEntity.ok(curriculumService.listByLanguage(languageCode));
    }

    @PostMapping
    public ResponseEntity<CfCurriculum> create(
            @Valid @RequestBody CreateCurriculumRequest req,
            @AuthenticationPrincipal UserDetails user) {

        if (curriculumService.findByStableRef(req.stableRef()).isPresent()) {
            throw new com.dsalearner.exception.ConflictException(
                    "Curriculum already exists: " + req.stableRef());
        }

        CfCurriculum curriculum = CfCurriculum.builder()
                .stableRef(req.stableRef())
                .domainCode(req.domainCode())
                .languageCode(req.languageCode())
                .displayName(req.displayName())
                .description(req.description())
                .batchSize(req.batchSize() > 0 ? req.batchSize() : 10)
                .createdBy(actorFrom(user))
                .build();

        CfCurriculum saved = curriculumService.save(curriculum);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CfCurriculum> get(@PathVariable UUID id) {
        return ResponseEntity.ok(curriculumService.getById(id));
    }

    // ─── Curriculum workflow transitions ──────────────────────────────────

    @PostMapping("/{id}/start-blueprint")
    public ResponseEntity<CfCurriculum> startBlueprint(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails user) {
        CfCurriculum curriculum = orchestrator.applyTransition(
                id, "BLUEPRINT_PENDING", "start_blueprint", actorFrom(user), null, null);
        // One job per CEFR level — each generates its LevelBlueprint independently.
        // The last job to complete assembles and persists the full CurriculumBlueprint.
        List<CfCurriculumLevel> levels = curriculumService.getLevels(id);
        for (CfCurriculumLevel level : levels) {
            jobWorker.enqueue(CfCurriculumPipelineJob.builder()
                    .curriculumId(id)
                    .levelId(level.getId())
                    .jobType("CURRICULUM_BLUEPRINT")
                    .payload(Map.of(
                            "cefrLevel",           level.getCefrLevel(),
                            "languageCode",        curriculum.getLanguageCode(),
                            "languageDisplayName", curriculum.getDisplayName(),
                            "script",              "Latin",
                            "domainCode",          curriculum.getDomainCode(),
                            "curriculumGoals",     ""
                    ))
                    .build());
        }
        return ResponseEntity.ok(curriculum);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<CfCurriculum> approve(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(orchestrator.approve(id, actorFrom(user)));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<CfCurriculum> archive(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(
                orchestrator.applyTransition(id, "ARCHIVED", "archive", actorFrom(user), null, null));
    }

    // ─── Levels ───────────────────────────────────────────────────────────

    @GetMapping("/{id}/levels")
    public ResponseEntity<List<CfCurriculumLevel>> listLevels(@PathVariable UUID id) {
        return ResponseEntity.ok(curriculumService.getLevels(id));
    }

    @PostMapping("/{id}/levels/{levelId}/start-generation")
    public ResponseEntity<CfCurriculumLevel> startLevelGeneration(
            @PathVariable UUID id,
            @PathVariable UUID levelId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(levelService.startGeneration(levelId, actorFrom(user)));
    }

    @PostMapping("/{id}/levels/{levelId}/approve")
    public ResponseEntity<CfCurriculumLevel> approveLevel(
            @PathVariable UUID id,
            @PathVariable UUID levelId,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(orchestrator.approveLevel(levelId, actorFrom(user)));
    }

    @PostMapping("/{id}/levels/{levelId}/qa")
    public ResponseEntity<CurriculumJobResponse> submitLevelQa(
            @PathVariable UUID id,
            @PathVariable UUID levelId,
            @AuthenticationPrincipal UserDetails user) {
        CfCurriculumPipelineJob job = jobWorker.enqueue(CfCurriculumPipelineJob.builder()
                .curriculumId(id)
                .levelId(levelId)
                .jobType("CURRICULUM_LEVEL_QA")
                .payload(Map.of())
                .build());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(CurriculumJobResponse.from(job));
    }

    // ─── Lesson plans ─────────────────────────────────────────────────────

    @GetMapping("/{id}/levels/{levelId}/plans")
    public ResponseEntity<List<CfCurriculumLessonPlan>> listPlans(
            @PathVariable UUID id,
            @PathVariable UUID levelId) {
        return ResponseEntity.ok(curriculumService.getPlansForLevel(levelId));
    }

    // ─── Coherence QA ─────────────────────────────────────────────────────

    @PostMapping("/{id}/coherence-qa")
    public ResponseEntity<CurriculumJobResponse> submitCoherenceQa(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails user) {
        CfCurriculumPipelineJob job = jobWorker.enqueue(CfCurriculumPipelineJob.builder()
                .curriculumId(id)
                .jobType("CURRICULUM_COHERENCE_QA")
                .payload(Map.of())
                .build());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(CurriculumJobResponse.from(job));
    }

    // ─── Jobs ─────────────────────────────────────────────────────────────

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<CurriculumJobResponse> getJob(@PathVariable UUID jobId) {
        CfCurriculumPipelineJob job = jobWorker.getJobRepository().findById(jobId)
                .orElseThrow(() -> new com.dsalearner.exception.NotFoundException(
                        "Curriculum job not found: " + jobId));
        return ResponseEntity.ok(CurriculumJobResponse.from(job));
    }

    // ─── Audit ────────────────────────────────────────────────────────────

    @GetMapping("/{id}/audit")
    public ResponseEntity<List<CfCurriculumWorkflowEvent>> auditCurriculum(@PathVariable UUID id) {
        return ResponseEntity.ok(eventRepository.findByCurriculumIdOrderByOccurredAtDesc(id));
    }

    @GetMapping("/{id}/levels/{levelId}/audit")
    public ResponseEntity<List<CfCurriculumWorkflowEvent>> auditLevel(
            @PathVariable UUID id,
            @PathVariable UUID levelId) {
        return ResponseEntity.ok(eventRepository.findByLevelIdOrderByOccurredAtDesc(levelId));
    }

    // ─── Health ───────────────────────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "curriculum-factory"));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    private String actorFrom(UserDetails user) {
        return user != null ? "admin:" + user.getUsername() : "system";
    }

    // ─── DTOs ─────────────────────────────────────────────────────────────

    record CreateCurriculumRequest(
            @NotBlank String stableRef,
            @NotBlank String domainCode,
            @NotBlank String languageCode,
            @NotBlank String displayName,
            String description,
            int batchSize
    ) {}

    record CurriculumJobResponse(
            UUID jobId,
            UUID curriculumId,
            UUID levelId,
            String jobType,
            String status,
            int attempt,
            int maxAttempts,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt,
            String resultReference,
            String error
    ) {
        static CurriculumJobResponse from(CfCurriculumPipelineJob job) {
            return new CurriculumJobResponse(
                    job.getId(), job.getCurriculumId(), job.getLevelId(),
                    job.getJobType(), job.getStatus(),
                    job.getAttempt(), job.getMaxAttempts(),
                    job.getCreatedAt(), job.getStartedAt(), job.getCompletedAt(),
                    job.getResultReference(), job.getError()
            );
        }
    }
}
