package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateRemovalRequestReasonService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.i18n.LocalizedEnumService;
import com.mefy.platemate.business.utilities.mappers.PlateRemovalRequestReasonAdminMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IPlateRemovalRequestReasonDao;
import com.mefy.platemate.entities.concrete.PlateRemovalRequestReason;
import com.mefy.platemate.entities.dto.PlateRemovalRequestReasonAdminDto;
import com.mefy.platemate.entities.dto.PlateRemovalRequestReasonDto;
import com.mefy.platemate.entities.dto.request.AddPlateRemovalRequestReasonRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateRemovalRequestReasonRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PlateRemovalRequestReasonManager implements IPlateRemovalRequestReasonService {

    private static final String ENUM_CATEGORY = "plate_removal_request_reason";

    private final IPlateRemovalRequestReasonDao plateRemovalRequestReasonDao;
    private final PlateRemovalRequestReasonAdminMapper plateRemovalRequestReasonAdminMapper;
    private final LocalizedEnumService localizedEnumService;
    private final IMessageService messageService;

    @Override
    public DataResult<List<PlateRemovalRequestReasonDto>> getActiveReasons() {
        List<PlateRemovalRequestReasonDto> reasons = plateRemovalRequestReasonDao.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(reason -> new PlateRemovalRequestReasonDto(
                        reason.getId(),
                        reason.getCode(),
                        localizedEnumService.label(ENUM_CATEGORY, reason.getCode(), reason.getLabel()),
                        reason.isRequiresDescription()
                ))
                .toList();
        return new SuccessDataResult<>(reasons, messageService.getMessage(Messages.PLATE_REMOVAL_REASONS_LISTED));
    }

    @Override
    public DataResult<List<PlateRemovalRequestReasonAdminDto>> getAllReasons() {
        List<PlateRemovalRequestReasonAdminDto> reasons = plateRemovalRequestReasonDao.findAllByOrderBySortOrderAsc()
                .stream()
                .map(plateRemovalRequestReasonAdminMapper::entityToDto)
                .toList();
        return new SuccessDataResult<>(reasons, messageService.getMessage(Messages.PLATE_REMOVAL_REASONS_LISTED));
    }

    @Override
    @Transactional
    public DataResult<PlateRemovalRequestReasonAdminDto> addReason(AddPlateRemovalRequestReasonRequest request) {
        String normalizedCode = normalizeCode(request.getCode());
        if (plateRemovalRequestReasonDao.existsByCode(normalizedCode)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.PLATE_REMOVAL_REASON_ALREADY_EXISTS));
        }

        LocalDateTime now = LocalDateTime.now();
        PlateRemovalRequestReason reason = new PlateRemovalRequestReason();
        reason.setCode(normalizedCode);
        reason.setLabel(request.getLabel().trim());
        reason.setRequiresDescription(Boolean.TRUE.equals(request.getRequiresDescription()));
        reason.setSortOrder(request.getSortOrder());
        reason.setActive(true);
        reason.setCreatedAt(now);
        reason.setUpdatedAt(now);
        plateRemovalRequestReasonDao.save(reason);

        return new SuccessDataResult<>(
                plateRemovalRequestReasonAdminMapper.entityToDto(reason),
                messageService.getMessage(Messages.PLATE_REMOVAL_REASON_ADDED)
        );
    }

    @Override
    @Transactional
    public DataResult<PlateRemovalRequestReasonAdminDto> updateReason(Long id, UpdatePlateRemovalRequestReasonRequest request) {
        PlateRemovalRequestReason reason = plateRemovalRequestReasonDao.findById(id).orElse(null);
        if (reason == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.PLATE_REMOVAL_REASON_NOT_FOUND));
        }

        String normalizedCode = normalizeCode(request.getCode());
        PlateRemovalRequestReason existingByCode = plateRemovalRequestReasonDao.findByCode(normalizedCode).orElse(null);
        if (existingByCode != null && !existingByCode.getId().equals(id)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.PLATE_REMOVAL_REASON_ALREADY_EXISTS));
        }

        reason.setCode(normalizedCode);
        reason.setLabel(request.getLabel().trim());
        reason.setRequiresDescription(Boolean.TRUE.equals(request.getRequiresDescription()));
        reason.setSortOrder(request.getSortOrder());
        reason.setUpdatedAt(LocalDateTime.now());
        plateRemovalRequestReasonDao.save(reason);

        return new SuccessDataResult<>(
                plateRemovalRequestReasonAdminMapper.entityToDto(reason),
                messageService.getMessage(Messages.PLATE_REMOVAL_REASON_UPDATED)
        );
    }

    @Override
    @Transactional
    public Result setReasonActive(Long id, boolean active) {
        PlateRemovalRequestReason reason = plateRemovalRequestReasonDao.findById(id).orElse(null);
        if (reason == null) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_REMOVAL_REASON_NOT_FOUND));
        }

        reason.setActive(active);
        reason.setUpdatedAt(LocalDateTime.now());
        plateRemovalRequestReasonDao.save(reason);

        return new SuccessResult(messageService.getMessage(Messages.PLATE_REMOVAL_REASON_STATUS_UPDATED));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }
}
