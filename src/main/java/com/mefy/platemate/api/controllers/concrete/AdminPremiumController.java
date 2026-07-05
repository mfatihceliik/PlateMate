package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IAdminPremiumController;
import com.mefy.platemate.business.abstracts.IAdminAccessService;
import com.mefy.platemate.business.abstracts.IPremiumService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PremiumFeatureAdminDto;
import com.mefy.platemate.entities.dto.PremiumPlanAdminDto;
import com.mefy.platemate.entities.dto.request.AddPremiumFeatureRequest;
import com.mefy.platemate.entities.dto.request.UpdatePremiumActiveRequest;
import com.mefy.platemate.entities.dto.request.UpdatePremiumFeatureRequest;
import com.mefy.platemate.entities.dto.request.UpdatePremiumPlanRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminPremiumController implements IAdminPremiumController {

    private final IAdminAccessService adminAccessService;
    private final IPremiumService premiumService;

    @Override
    public ResponseEntity<DataResult<List<PremiumPlanAdminDto>>> getAllPlans(Long currentUserId) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDataResult<>(authResult.getMessage()));
        }
        DataResult<List<PremiumPlanAdminDto>> result = premiumService.getAllPlans();
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<PremiumPlanAdminDto>> updatePlan(Long id, Long currentUserId, UpdatePremiumPlanRequest request) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDataResult<>(authResult.getMessage()));
        }
        DataResult<PremiumPlanAdminDto> result = premiumService.updatePlan(id, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> setPlanActive(Long id, Long currentUserId, UpdatePremiumActiveRequest request) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResult);
        }
        Result result = premiumService.setPlanActive(id, request.getActive());
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<List<PremiumFeatureAdminDto>>> getAllFeatures(Long currentUserId) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDataResult<>(authResult.getMessage()));
        }
        DataResult<List<PremiumFeatureAdminDto>> result = premiumService.getAllFeatures();
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<DataResult<PremiumFeatureAdminDto>> addFeature(Long currentUserId, AddPremiumFeatureRequest request) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDataResult<>(authResult.getMessage()));
        }
        DataResult<PremiumFeatureAdminDto> result = premiumService.addFeature(request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Override
    public ResponseEntity<DataResult<PremiumFeatureAdminDto>> updateFeature(Long id, Long currentUserId, UpdatePremiumFeatureRequest request) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDataResult<>(authResult.getMessage()));
        }
        DataResult<PremiumFeatureAdminDto> result = premiumService.updateFeature(id, request);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Result> setFeatureActive(Long id, Long currentUserId, UpdatePremiumActiveRequest request) {
        Result authResult = adminAccessService.checkAdmin(currentUserId);
        if (!authResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authResult);
        }
        Result result = premiumService.setFeatureActive(id, request.getActive());
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
