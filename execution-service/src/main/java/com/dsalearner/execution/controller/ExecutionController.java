package com.dsalearner.execution.controller;

import com.dsalearner.execution.model.ExecutionRequest;
import com.dsalearner.execution.model.ExecutionResult;
import com.dsalearner.execution.service.JavaExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/execute")
@RequiredArgsConstructor
public class ExecutionController {

    private final JavaExecutionService executionService;

    @PostMapping
    public ResponseEntity<ExecutionResult> execute(@Valid @RequestBody ExecutionRequest request) {
        return ResponseEntity.ok(executionService.execute(request));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UP");
    }
}
