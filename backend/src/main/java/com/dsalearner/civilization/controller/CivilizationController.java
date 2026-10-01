package com.dsalearner.civilization.controller;

import com.dsalearner.academy.security.CurrentUserProvider;
import com.dsalearner.civilization.dto.*;
import com.dsalearner.civilization.repository.LangoaBuildingInstanceRepository;
import com.dsalearner.civilization.repository.LangoaBuildingDefinitionRepository;
import com.dsalearner.civilization.service.CivilizationService;
import com.dsalearner.security.DomainAuthorizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ConditionalOnExpression("'${application.mode}' == 'language' or '${application.mode}' == 'all'")
@RestController
@RequestMapping("/api/v1/civilization/{language}")
@RequiredArgsConstructor
public class CivilizationController {

    private final CivilizationService civilizationService;
    private final CurrentUserProvider currentUserProvider;
    private final DomainAuthorizationService domainAuthService;
    private final LangoaBuildingInstanceRepository buildingInstanceRepo;
    private final LangoaBuildingDefinitionRepository buildingDefRepo;

    /**
     * GET /api/v1/civilization/{language}
     * Returns the full civilization state, creating it if it does not yet exist.
     */
    @GetMapping
    public ResponseEntity<CivilizationStateResponse> getCivilizationState(
            @PathVariable String language,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(civilizationService.getOrCreateAndGetState(userId, language));
    }

    /**
     * GET /api/v1/civilization/{language}/buildings/definitions
     * Returns all active building definitions with affordability flags.
     */
    @GetMapping("/buildings/definitions")
    public ResponseEntity<List<BuildingDefinitionDto>> getBuildingDefinitions(
            @PathVariable String language,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(civilizationService.getBuildingDefinitions(userId, language));
    }

    /**
     * POST /api/v1/civilization/{language}/buildings
     * Purchase and place a new building.
     */
    @PostMapping("/buildings")
    public ResponseEntity<CivilizationStateResponse> buildBuilding(
            @PathVariable String language,
            @Valid @RequestBody BuildBuildingRequest request,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(civilizationService.buildBuilding(userId, language, request));
    }

    /**
     * GET /api/v1/civilization/{language}/buildings
     * Returns all building instances for the user's civilization.
     */
    @GetMapping("/buildings")
    public ResponseEntity<List<BuildingInstanceDto>> getBuildings(
            @PathVariable String language,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);

        // Ensure civilization exists first
        var civ = civilizationService.getOrCreateCivilization(userId, language);

        // Build display name map
        java.util.Map<String, String> defDisplayNames = buildingDefRepo.findByActiveTrueOrderByDisplayOrder()
                .stream()
                .collect(Collectors.toMap(
                        com.dsalearner.civilization.model.entity.LangoaBuildingDefinition::getBuildingType,
                        com.dsalearner.civilization.model.entity.LangoaBuildingDefinition::getDisplayName));

        List<BuildingInstanceDto> buildings = buildingInstanceRepo.findByCivilizationId(civ.getId())
                .stream()
                .map(inst -> new BuildingInstanceDto(
                        inst.getId(),
                        inst.getBuildingType(),
                        defDisplayNames.getOrDefault(inst.getBuildingType(), inst.getBuildingType()),
                        inst.getCurrentLevel(),
                        inst.getPositionX(),
                        inst.getPositionY()))
                .toList();

        return ResponseEntity.ok(buildings);
    }
}
