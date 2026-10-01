package com.dsalearner.academy.controller;

import com.dsalearner.academy.service.CurriculumValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ConditionalOnExpression("'${application.mode}' == 'language' or '${application.mode}' == 'all'")
@RestController
@RequestMapping("/api/v1/curriculum")
@RequiredArgsConstructor
public class CurriculumValidatorController {

    private final CurriculumValidatorService validatorService;

    @GetMapping("/{language}/validate")
    public ResponseEntity<CurriculumValidatorService.ValidationReport> validate(
            @PathVariable String language) {
        return ResponseEntity.ok(validatorService.validate(language));
    }
}
