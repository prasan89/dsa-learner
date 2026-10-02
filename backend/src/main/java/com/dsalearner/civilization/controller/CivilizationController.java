package com.dsalearner.civilization.controller;

import com.dsalearner.academy.security.CurrentUserProvider;
import com.dsalearner.civilization.dto.*;
import com.dsalearner.civilization.repository.LangoaBuildingDefinitionRepository;
import com.dsalearner.civilization.repository.LangoaBuildingInstanceRepository;
import com.dsalearner.civilization.repository.LangoaCurrencyBalanceRepository;
import com.dsalearner.civilization.repository.LangoaTransactionRepository;
import com.dsalearner.civilization.service.AchievementService;
import com.dsalearner.civilization.service.CivilizationService;
import com.dsalearner.civilization.service.QuestService;
import com.dsalearner.security.DomainAuthorizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ConditionalOnExpression("'${application.mode}' == 'language' or '${application.mode}' == 'all'")
@RestController
@RequestMapping("/api/v1/civilization/{language}")
@RequiredArgsConstructor
public class CivilizationController {

    private final CivilizationService civilizationService;
    private final QuestService questService;
    private final AchievementService achievementService;
    private final CurrentUserProvider currentUserProvider;
    private final DomainAuthorizationService domainAuthService;
    private final LangoaBuildingInstanceRepository buildingInstanceRepo;
    private final LangoaBuildingDefinitionRepository buildingDefRepo;
    private final LangoaCurrencyBalanceRepository balanceRepo;
    private final LangoaTransactionRepository transactionRepo;

    @GetMapping
    public ResponseEntity<CivilizationStateResponse> getCivilizationState(
            @PathVariable String language,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(civilizationService.getOrCreateAndGetState(userId, language));
    }

    @GetMapping("/buildings/definitions")
    public ResponseEntity<List<BuildingDefinitionDto>> getBuildingDefinitions(
            @PathVariable String language,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(civilizationService.getBuildingDefinitions(userId, language));
    }

    @PostMapping("/buildings")
    public ResponseEntity<CivilizationStateResponse> buildBuilding(
            @PathVariable String language,
            @Valid @RequestBody BuildBuildingRequest request,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(civilizationService.buildBuilding(userId, language, request));
    }

    @GetMapping("/buildings")
    public ResponseEntity<List<BuildingInstanceDto>> getBuildings(
            @PathVariable String language,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);

        var civ = civilizationService.getOrCreateCivilization(userId, language);

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
                        inst.getPositionY(),
                        inst.getBuildState(),
                        inst.getRotationDeg(),
                        inst.getWidthTiles(),
                        inst.getHeightTiles()))
                .toList();

        return ResponseEntity.ok(buildings);
    }

    @PostMapping("/buildings/{buildingInstanceId}/upgrade")
    public ResponseEntity<CivilizationStateResponse> upgradeBuilding(
            @PathVariable String language,
            @PathVariable UUID buildingInstanceId,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(civilizationService.upgradeBuilding(userId, language, buildingInstanceId));
    }

    @PostMapping("/buildings/{buildingInstanceId}/move")
    public ResponseEntity<CivilizationStateResponse> moveBuilding(
            @PathVariable String language,
            @PathVariable UUID buildingInstanceId,
            @Valid @RequestBody MoveBuildingRequest request,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(civilizationService.moveBuilding(userId, language, buildingInstanceId, request));
    }

    @GetMapping("/decorations/definitions")
    public ResponseEntity<List<DecorationDefinitionDto>> getDecorationDefinitions(
            @PathVariable String language,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(civilizationService.getDecorationDefinitions(userId, language));
    }

    @PostMapping("/decorations")
    public ResponseEntity<CivilizationStateResponse> placeDecoration(
            @PathVariable String language,
            @Valid @RequestBody PlaceDecorationRequest request,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(civilizationService.placeDecoration(userId, language, request));
    }

    @GetMapping("/expansions")
    public ResponseEntity<List<CityExpansionDto>> getExpansions(
            @PathVariable String language,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(civilizationService.getExpansions(userId, language));
    }

    @PostMapping("/expansions/{slot}")
    public ResponseEntity<CityExpansionDto> purchaseExpansion(
            @PathVariable String language,
            @PathVariable int slot,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(civilizationService.purchaseExpansion(userId, language, slot));
    }

    @GetMapping("/quests")
    public ResponseEntity<List<QuestDto>> getQuests(
            @PathVariable String language,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(questService.getActiveQuests(userId, language));
    }

    @GetMapping("/achievements")
    public ResponseEntity<List<AchievementDto>> getAchievements(
            @PathVariable String language,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(achievementService.getAchievements(userId, language));
    }

    @GetMapping("/wallet")
    public ResponseEntity<WalletResponse> getWallet(
            @PathVariable String language,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);

        String resolvedCode = civilizationService.resolveLanguageCode(language);

        Map<String, Long> balances = balanceRepo.findByUserIdAndLanguageCode(userId, resolvedCode)
                .stream()
                .collect(Collectors.toMap(
                        b -> b.getCurrencyType().name(),
                        b -> b.getBalance()));

        List<WalletResponse.TransactionSummary> recentTxns = transactionRepo
                .findTop20ByUserIdAndLanguageCodeOrderByCreatedAtDesc(userId, resolvedCode)
                .stream()
                .map(t -> new WalletResponse.TransactionSummary(
                        t.getTransactionType().name(),
                        t.getCurrencyType().name(),
                        t.getAmount(),
                        t.getBalanceAfter(),
                        t.getCreatedAt()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new WalletResponse(balances, recentTxns));
    }

    @PostMapping("/resources/collect")
    public ResponseEntity<CivilizationStateResponse> collectResources(
            @PathVariable String language,
            Authentication authentication) {
        domainAuthService.requireDomain(authentication, "language");
        UUID userId = currentUserProvider.getUserId(authentication);
        return ResponseEntity.ok(civilizationService.collectAllResources(userId, language));
    }
}
