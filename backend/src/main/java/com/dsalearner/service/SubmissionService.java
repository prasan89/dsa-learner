package com.dsalearner.service;

import com.dsalearner.dto.request.CodeExecutionRequest;
import com.dsalearner.dto.response.RunResultResponse;
import com.dsalearner.dto.response.SubmissionResponse;
import com.dsalearner.exception.NotFoundException;
import com.dsalearner.model.entity.*;
import com.dsalearner.model.enums.SubmissionStatus;
import com.dsalearner.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final TestCaseRepository testCaseRepository;
    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${app.execution-service.url}")
    private String executionServiceUrl;

    @Value("${app.execution-service.timeout-ms:10000}")
    private int executionTimeoutMs;

    public RunResultResponse run(String problemSlug, CodeExecutionRequest req, UUID userId) {
        Problem problem = problemRepository.findBySlugAndActiveTrue(problemSlug)
                .orElseThrow(() -> new NotFoundException("Problem not found: " + problemSlug));

        List<TestCase> testCases = testCaseRepository.findAllByProblemOrderByDisplayOrderAsc(problem);
        List<TestCase> visibleCases = testCases.stream().filter(tc -> !tc.isHidden()).toList();

        return callExecutionService(req.code(), visibleCases);
    }

    @Transactional
    public SubmissionResponse submit(String problemSlug, CodeExecutionRequest req, UUID userId) {
        Problem problem = problemRepository.findBySlugAndActiveTrue(problemSlug)
                .orElseThrow(() -> new NotFoundException("Problem not found: " + problemSlug));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<TestCase> allCases = testCaseRepository.findAllByProblemOrderByDisplayOrderAsc(problem);
        RunResultResponse runResult = callExecutionService(req.code(), allCases);

        SubmissionStatus status = SubmissionStatus.valueOf(runResult.status());

        Submission submission = Submission.builder()
                .user(user)
                .problem(problem)
                .code(req.code())
                .language(req.language())
                .status(status)
                .runtimeMs(runResult.runtimeMs())
                .errorMessage(runResult.errorMessage())
                .build();
        submissionRepository.save(submission);

        if (status == SubmissionStatus.ACCEPTED) {
            updateProgress(user, problem);
        }

        return toResponse(submission);
    }

    public List<SubmissionResponse> listForUser(UUID userId) {
        return submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    public List<SubmissionResponse> listForProblem(String problemSlug, UUID userId) {
        Problem problem = problemRepository.findBySlugAndActiveTrue(problemSlug)
                .orElseThrow(() -> new NotFoundException("Problem not found: " + problemSlug));

        return submissionRepository
                .findByUserIdAndProblemIdOrderBySubmittedAtDesc(userId, problem.getId())
                .stream().map(this::toResponse).toList();
    }

    private RunResultResponse callExecutionService(String code, List<TestCase> testCases) {
        List<Map<String, String>> tcList = testCases.stream().map(tc -> Map.of(
                "id", tc.getId().toString(),
                "input", tc.getInput(),
                "expectedOutput", tc.getExpectedOutput()
        )).toList();

        Map<String, Object> payload = Map.of(
                "code", code,
                "language", "JAVA",
                "testCases", tcList
        );

        try {
            WebClient client = webClientBuilder.baseUrl(executionServiceUrl).build();
            Map<?, ?> result = client.post()
                    .uri("/execute")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(java.time.Duration.ofMillis(executionTimeoutMs));

            if (result == null) {
                return new RunResultResponse("RUNTIME_ERROR", "No response from execution service", null, List.of());
            }

            List<RunResultResponse.TestResultResponse> testResults = new ArrayList<>();
            if (result.get("testResults") instanceof List<?> rawList) {
                for (Object item : rawList) {
                    if (item instanceof Map<?, ?> m) {
                        testResults.add(new RunResultResponse.TestResultResponse(
                                (String) m.get("testCaseId"),
                                Boolean.TRUE.equals(m.get("passed")),
                                (String) m.get("input"),
                                (String) m.get("expectedOutput"),
                                (String) m.get("actualOutput"),
                                m.get("executionTimeMs") instanceof Number n ? n.intValue() : null
                        ));
                    }
                }
            }

            return new RunResultResponse(
                    (String) result.get("status"),
                    (String) result.get("errorMessage"),
                    result.get("runtimeMs") instanceof Number n ? n.intValue() : null,
                    testResults
            );
        } catch (Exception e) {
            log.error("Execution service call failed", e);
            return new RunResultResponse("RUNTIME_ERROR", "Execution service unavailable: " + e.getMessage(), null, List.of());
        }
    }

    private void updateProgress(User user, Problem problem) {
        UserProgress progress = userProgressRepository
                .findByUserIdAndProblemId(user.getId(), problem.getId())
                .orElseGet(() -> UserProgress.builder().user(user).problem(problem).build());

        progress.setSolved(true);
        progress.setAttempts(progress.getAttempts() + 1);
        if (progress.getSolvedAt() == null) progress.setSolvedAt(Instant.now());
        userProgressRepository.save(progress);
    }

    private SubmissionResponse toResponse(Submission s) {
        return new SubmissionResponse(
                s.getId(), s.getProblem().getId(), s.getProblem().getSlug(),
                s.getProblem().getTitle(), s.getStatus(), s.getLanguage(),
                s.getCode(), s.getRuntimeMs(), s.getMemoryKb(),
                s.getErrorMessage(), s.getSubmittedAt());
    }
}
