package com.dsalearner.execution.service;

import com.dsalearner.execution.model.ExecutionRequest;
import com.dsalearner.execution.model.ExecutionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class JavaExecutionService {

    @Value("${execution.timeout-ms:5000}")
    private int timeoutMs;

    @Value("${execution.work-dir:/tmp/sandbox}")
    private String workDir;

    public ExecutionResult execute(ExecutionRequest request) {
        String runId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        Path dir = Path.of(workDir, runId);

        try {
            Files.createDirectories(dir);
            Path sourceFile = dir.resolve("Solution.java");
            Files.writeString(sourceFile, request.getCode());

            // Compile
            String compileError = compile(dir, sourceFile);
            if (compileError != null) {
                return ExecutionResult.builder()
                        .status("COMPILATION_ERROR")
                        .errorMessage(compileError)
                        .testResults(List.of())
                        .build();
            }

            // Run each test case
            List<ExecutionResult.TestCaseResult> testResults = new ArrayList<>();
            long totalStart = System.currentTimeMillis();
            boolean allPassed = true;

            for (ExecutionRequest.TestCaseInput tc : request.getTestCases()) {
                ExecutionResult.TestCaseResult result = runTestCase(dir, tc, request.getTimeLimitMs());
                testResults.add(result);
                if (!result.isPassed()) allPassed = false;
            }

            long totalTime = System.currentTimeMillis() - totalStart;
            String status = allPassed ? "ACCEPTED" : "WRONG_ANSWER";

            // Check if any test case hit TLE
            boolean tle = testResults.stream()
                    .anyMatch(r -> "TIME_LIMIT_EXCEEDED".equals(r.getActualOutput()));
            if (tle) status = "TIME_LIMIT_EXCEEDED";

            return ExecutionResult.builder()
                    .status(status)
                    .runtimeMs((int) totalTime)
                    .testResults(testResults)
                    .build();

        } catch (Exception e) {
            log.error("Execution error for runId {}", runId, e);
            return ExecutionResult.builder()
                    .status("RUNTIME_ERROR")
                    .errorMessage(e.getMessage())
                    .testResults(List.of())
                    .build();
        } finally {
            deleteDirectory(dir);
        }
    }

    private String compile(Path dir, Path sourceFile) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("javac", sourceFile.toString())
                .directory(dir.toFile())
                .redirectErrorStream(true);

        Process p = pb.start();
        String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        boolean finished = p.waitFor(30, TimeUnit.SECONDS);

        if (!finished || p.exitValue() != 0) {
            return output.isBlank() ? "Compilation failed" : output;
        }
        return null;
    }

    private ExecutionResult.TestCaseResult runTestCase(Path dir,
                                                        ExecutionRequest.TestCaseInput tc,
                                                        int timeLimitMs) {
        long start = System.currentTimeMillis();
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", dir.toString(), "Solution")
                    .directory(dir.toFile());
            pb.redirectErrorStream(false);

            Process p = pb.start();

            // Write input to stdin
            try (OutputStream stdin = p.getOutputStream()) {
                stdin.write(tc.getInput().getBytes(StandardCharsets.UTF_8));
            }

            boolean finished = p.waitFor(timeLimitMs, TimeUnit.MILLISECONDS);
            long elapsed = System.currentTimeMillis() - start;

            if (!finished) {
                p.destroyForcibly();
                return ExecutionResult.TestCaseResult.builder()
                        .testCaseId(tc.getId())
                        .passed(false)
                        .input(tc.getInput())
                        .expectedOutput(tc.getExpectedOutput())
                        .actualOutput("TIME_LIMIT_EXCEEDED")
                        .executionTimeMs((int) elapsed)
                        .build();
            }

            String stdout = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            String stderr = new String(p.getErrorStream().readAllBytes(), StandardCharsets.UTF_8).trim();

            if (p.exitValue() != 0) {
                return ExecutionResult.TestCaseResult.builder()
                        .testCaseId(tc.getId())
                        .passed(false)
                        .input(tc.getInput())
                        .expectedOutput(tc.getExpectedOutput())
                        .actualOutput("RUNTIME_ERROR: " + stderr)
                        .executionTimeMs((int) elapsed)
                        .build();
            }

            boolean passed = stdout.equals(tc.getExpectedOutput().trim());
            return ExecutionResult.TestCaseResult.builder()
                    .testCaseId(tc.getId())
                    .passed(passed)
                    .input(tc.getInput())
                    .expectedOutput(tc.getExpectedOutput())
                    .actualOutput(stdout)
                    .executionTimeMs((int) elapsed)
                    .build();

        } catch (Exception e) {
            return ExecutionResult.TestCaseResult.builder()
                    .testCaseId(tc.getId())
                    .passed(false)
                    .input(tc.getInput())
                    .expectedOutput(tc.getExpectedOutput())
                    .actualOutput("ERROR: " + e.getMessage())
                    .executionTimeMs((int) (System.currentTimeMillis() - start))
                    .build();
        }
    }

    private void deleteDirectory(Path dir) {
        try {
            if (Files.exists(dir)) {
                try (var stream = Files.walk(dir)) {
                    stream.sorted((a, b) -> b.compareTo(a))
                          .forEach(p -> { try { Files.delete(p); } catch (IOException ignored) {} });
                }
            }
        } catch (IOException ignored) {}
    }
}
