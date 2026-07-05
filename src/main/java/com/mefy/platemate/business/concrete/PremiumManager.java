package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPremiumService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IPremiumFeatureDao;
import com.mefy.platemate.dataAccess.abstracts.IPremiumPlanDao;
import com.mefy.platemate.entities.concrete.PremiumFeature;
import com.mefy.platemate.entities.concrete.PremiumPlan;
import com.mefy.platemate.entities.dto.PremiumCatalogDto;
import com.mefy.platemate.entities.dto.PremiumFeatureAdminDto;
import com.mefy.platemate.entities.dto.PremiumFeatureDto;
import com.mefy.platemate.entities.dto.PremiumPlanAdminDto;
import com.mefy.platemate.entities.dto.PremiumPlanDto;
import com.mefy.platemate.entities.dto.request.AddPremiumFeatureRequest;
import com.mefy.platemate.entities.dto.request.UpdatePremiumFeatureRequest;
import com.mefy.platemate.entities.dto.request.UpdatePremiumPlanRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PremiumManager implements IPremiumService {
    private final IPremiumPlanDao premiumPlanDao;
    private final IPremiumFeatureDao premiumFeatureDao;
    private final IMessageService messageService;

    // ---- Public ----

    @Override
    public DataResult<PremiumCatalogDto> getCatalog() {
        List<PremiumPlanDto> plans = premiumPlanDao.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(this::toPlanDto)
                .toList();
        List<PremiumFeatureDto> features = premiumFeatureDao.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(this::toFeatureDto)
                .toList();
        PremiumCatalogDto catalog = new PremiumCatalogDto(plans, features);
        return new SuccessDataResult<>(catalog, messageService.getMessage(Messages.PREMIUM_CATALOG_LOADED));
    }

    // ---- Admin: plans ----

    @Override
    public DataResult<List<PremiumPlanAdminDto>> getAllPlans() {
        List<PremiumPlanAdminDto> data = premiumPlanDao.findAllByOrderBySortOrderAsc().stream()
                .map(this::toPlanAdminDto)
                .toList();
        return new SuccessDataResult<>(data, messageService.getMessage(Messages.PREMIUM_PLANS_LISTED));
    }

    @Override
    @Transactional
    public DataResult<PremiumPlanAdminDto> updatePlan(Long id, UpdatePremiumPlanRequest request) {
        PremiumPlan existing = premiumPlanDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.PREMIUM_PLAN_NOT_FOUND));
        }

        existing.setAmount(request.getAmount());
        existing.setCurrency(request.getCurrency().trim().toUpperCase(Locale.ROOT));
        existing.setDiscountPercent(request.getDiscountPercent());
        existing.setSortOrder(request.getSortOrder());
        existing.setUpdatedAt(LocalDateTime.now());

        PremiumPlan updated = premiumPlanDao.save(existing);
        return new SuccessDataResult<>(toPlanAdminDto(updated), messageService.getMessage(Messages.PREMIUM_PLAN_UPDATED));
    }

    @Override
    @Transactional
    public Result setPlanActive(Long id, boolean active) {
        PremiumPlan existing = premiumPlanDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorResult(messageService.getMessage(Messages.PREMIUM_PLAN_NOT_FOUND));
        }
        existing.setActive(active);
        existing.setUpdatedAt(LocalDateTime.now());
        premiumPlanDao.save(existing);
        return new SuccessResult(messageService.getMessage(Messages.PREMIUM_PLAN_STATUS_UPDATED));
    }

    // ---- Admin: features ----

    @Override
    public DataResult<List<PremiumFeatureAdminDto>> getAllFeatures() {
        List<PremiumFeatureAdminDto> data = premiumFeatureDao.findAllByOrderBySortOrderAsc().stream()
                .map(this::toFeatureAdminDto)
                .toList();
        return new SuccessDataResult<>(data, messageService.getMessage(Messages.PREMIUM_FEATURES_LISTED));
    }

    @Override
    @Transactional
    public DataResult<PremiumFeatureAdminDto> addFeature(AddPremiumFeatureRequest request) {
        LocalDateTime now = LocalDateTime.now();
        PremiumFeature feature = new PremiumFeature();
        applyFeatureFields(feature, request.getIconKey(), request.getTitleTr(), request.getTitleEn(),
                request.getSubtitleTr(), request.getSubtitleEn(), request.getSortOrder());
        feature.setActive(true);
        feature.setCreatedAt(now);
        feature.setUpdatedAt(now);

        PremiumFeature saved = premiumFeatureDao.save(feature);
        return new SuccessDataResult<>(toFeatureAdminDto(saved), messageService.getMessage(Messages.PREMIUM_FEATURE_ADDED));
    }

    @Override
    @Transactional
    public DataResult<PremiumFeatureAdminDto> updateFeature(Long id, UpdatePremiumFeatureRequest request) {
        PremiumFeature existing = premiumFeatureDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.PREMIUM_FEATURE_NOT_FOUND));
        }
        applyFeatureFields(existing, request.getIconKey(), request.getTitleTr(), request.getTitleEn(),
                request.getSubtitleTr(), request.getSubtitleEn(), request.getSortOrder());
        existing.setUpdatedAt(LocalDateTime.now());

        PremiumFeature updated = premiumFeatureDao.save(existing);
        return new SuccessDataResult<>(toFeatureAdminDto(updated), messageService.getMessage(Messages.PREMIUM_FEATURE_UPDATED));
    }

    @Override
    @Transactional
    public Result setFeatureActive(Long id, boolean active) {
        PremiumFeature existing = premiumFeatureDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorResult(messageService.getMessage(Messages.PREMIUM_FEATURE_NOT_FOUND));
        }
        existing.setActive(active);
        existing.setUpdatedAt(LocalDateTime.now());
        premiumFeatureDao.save(existing);
        return new SuccessResult(messageService.getMessage(Messages.PREMIUM_FEATURE_STATUS_UPDATED));
    }

    // ---- Mapping / helpers ----

    private void applyFeatureFields(
            PremiumFeature feature,
            String iconKey,
            String titleTr,
            String titleEn,
            String subtitleTr,
            String subtitleEn,
            Integer sortOrder
    ) {
        feature.setIconKey(iconKey.trim().toLowerCase(Locale.ROOT));
        feature.setTitleTr(titleTr.trim());
        feature.setTitleEn(titleEn.trim());
        feature.setSubtitleTr(blankToNull(subtitleTr));
        feature.setSubtitleEn(blankToNull(subtitleEn));
        feature.setSortOrder(sortOrder);
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private PremiumPlanDto toPlanDto(PremiumPlan plan) {
        return new PremiumPlanDto(
                plan.getId(),
                plan.getPeriod(),
                plan.getAmount(),
                plan.getCurrency(),
                plan.getDiscountPercent(),
                plan.getSortOrder()
        );
    }

    private PremiumPlanAdminDto toPlanAdminDto(PremiumPlan plan) {
        return new PremiumPlanAdminDto(
                plan.getId(),
                plan.getPeriod(),
                plan.getAmount(),
                plan.getCurrency(),
                plan.getDiscountPercent(),
                plan.getSortOrder(),
                plan.getActive(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }

    private PremiumFeatureDto toFeatureDto(PremiumFeature feature) {
        return new PremiumFeatureDto(
                feature.getId(),
                feature.getIconKey(),
                feature.getTitleTr(),
                feature.getTitleEn(),
                feature.getSubtitleTr(),
                feature.getSubtitleEn(),
                feature.getSortOrder()
        );
    }

    private PremiumFeatureAdminDto toFeatureAdminDto(PremiumFeature feature) {
        return new PremiumFeatureAdminDto(
                feature.getId(),
                feature.getIconKey(),
                feature.getTitleTr(),
                feature.getTitleEn(),
                feature.getSubtitleTr(),
                feature.getSubtitleEn(),
                feature.getSortOrder(),
                feature.getActive(),
                feature.getCreatedAt(),
                feature.getUpdatedAt()
        );
    }
}
