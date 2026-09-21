package com.dsalearner.controller;

import com.dsalearner.dto.response.PatternResponse;
import com.dsalearner.service.PatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patterns")
@RequiredArgsConstructor
public class PatternController {

    private final PatternService patternService;

    @GetMapping
    public ResponseEntity<List<PatternResponse>> list() {
        return ResponseEntity.ok(patternService.findAll());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PatternResponse> get(@PathVariable String slug) {
        return ResponseEntity.ok(patternService.findBySlug(slug));
    }
}
