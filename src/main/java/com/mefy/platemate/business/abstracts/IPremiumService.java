package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.PremiumCatalogDto;
import com.mefy.platemate.entities.dto.PremiumFeatureAdminDto;
import com.mefy.platemate.entities.dto.PremiumPlanAdminDto;
import com.mefy.platemate.entities.dto.request.AddPremiumFeatureRequest;
import com.mefy.platemate.entities.dto.request.UpdatePremiumFeatureRequest;
import com.mefy.platemate.entities.dto.request.UpdatePremiumPlanRequest;

import java.util.List;

public interface IPremiumService {
    // Public read (active plans + active features).
    DataResult<PremiumCatalogDto> getCatalog();

    // Admin — plans (edit-only; the seeded MONTHLY/YEARLY rows).
    DataResult<List<PremiumPlanAdminDto>> getAllPlans();

    DataResult<PremiumPlanAdminDto> updatePlan(Long id, UpdatePremiumPlanRequest request);

    Result setPlanActive(Long id, boolean active);

    // Admin — features (full CRUD).
    DataResult<List<PremiumFeatureAdminDto>> getAllFeatures();

    DataResult<PremiumFeatureAdminDto> addFeature(AddPremiumFeatureRequest request);

    DataResult<PremiumFeatureAdminDto> updateFeature(Long id, UpdatePremiumFeatureRequest request);

    Result setFeatureActive(Long id, boolean active);
}
