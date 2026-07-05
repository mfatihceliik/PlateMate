package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PremiumFeatureAdminDto;
import com.mefy.platemate.entities.dto.PremiumPlanAdminDto;
import com.mefy.platemate.entities.dto.request.AddPremiumFeatureRequest;
import com.mefy.platemate.entities.dto.request.UpdatePremiumActiveRequest;
import com.mefy.platemate.entities.dto.request.UpdatePremiumFeatureRequest;
import com.mefy.platemate.entities.dto.request.UpdatePremiumPlanRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/admin/premium")
public interface IAdminPremiumController {

    // Plans (edit-only).
    @GetMapping("/plans")
    ResponseEntity<DataResult<List<PremiumPlanAdminDto>>> getAllPlans(
            @RequestAttribute("userId") Long currentUserId
    );

    @PutMapping("/plans/{id}")
    ResponseEntity<DataResult<PremiumPlanAdminDto>> updatePlan(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdatePremiumPlanRequest request
    );

    @PatchMapping("/plans/{id}/active")
    ResponseEntity<Result> setPlanActive(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdatePremiumActiveRequest request
    );

    // Features (full CRUD).
    @GetMapping("/features")
    ResponseEntity<DataResult<List<PremiumFeatureAdminDto>>> getAllFeatures(
            @RequestAttribute("userId") Long currentUserId
    );

    @PostMapping("/features")
    ResponseEntity<DataResult<PremiumFeatureAdminDto>> addFeature(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddPremiumFeatureRequest request
    );

    @PutMapping("/features/{id}")
    ResponseEntity<DataResult<PremiumFeatureAdminDto>> updateFeature(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdatePremiumFeatureRequest request
    );

    @PatchMapping("/features/{id}/active")
    ResponseEntity<Result> setFeatureActive(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdatePremiumActiveRequest request
    );
}
