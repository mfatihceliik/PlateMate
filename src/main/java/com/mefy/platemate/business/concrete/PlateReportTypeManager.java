package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IPlateReportTypeService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReportTypeDao;
import com.mefy.platemate.entities.concrete.PlateReportType;
import com.mefy.platemate.entities.dto.PlateReportTypeAdminDto;
import com.mefy.platemate.entities.dto.PlateReportTypeDto;
import com.mefy.platemate.entities.dto.request.AddPlateReportTypeRequest;
import com.mefy.platemate.entities.dto.request.UpdatePlateReportTypeRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PlateReportTypeManager implements IPlateReportTypeService {

    private final IPlateReportDao plateReportDao;
    private final IPlateReportTypeDao plateReportTypeDao;
    private final IMessageService messageService;

    @Override
    public DataResult<List<PlateReportTypeDto>> getActiveReportTypes() {
        List<PlateReportTypeDto> data = plateReportTypeDao.findByActiveTrueOrderBySortOrderAsc().stream()
                .map(this::toPublicDto)
                .toList();
        return new SuccessDataResult<>(data, messageService.getMessage(Messages.PLATE_REPORT_TYPES_LISTED));
    }

    @Override
    public DataResult<List<PlateReportTypeAdminDto>> getAllReportTypes() {
        List<PlateReportTypeAdminDto> data = plateReportTypeDao.findAllByOrderBySortOrderAsc().stream()
                .map(this::toAdminDto)
                .toList();
        return new SuccessDataResult<>(data, messageService.getMessage(Messages.PLATE_REPORT_TYPES_LISTED));
    }

    @Override
    @Transactional
    public DataResult<PlateReportTypeAdminDto> addReportType(AddPlateReportTypeRequest request) {
        String normalizedCode = normalizeCode(request.getCode());
        if (plateReportTypeDao.existsByCode(normalizedCode)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.REPORT_TYPE_ALREADY_EXISTS));
        }

        LocalDateTime now = LocalDateTime.now();
        PlateReportType type = new PlateReportType();
        type.setCode(normalizedCode);
        applyFields(type, request.getLabel(), request.getDescription(), request.getIconKey(), request.getSeverity(), request.getColorHex(), request.getWeight(), request.getSortOrder());
        type.setActive(true);
        type.setCreatedAt(now);
        type.setUpdatedAt(now);

        PlateReportType saved = plateReportTypeDao.save(type);
        return new SuccessDataResult<>(toAdminDto(saved), messageService.getMessage(Messages.PLATE_REPORT_TYPE_ADDED));
    }

    @Override
    @Transactional
    public DataResult<PlateReportTypeAdminDto> updateReportType(Long id, UpdatePlateReportTypeRequest request) {
        PlateReportType existing = plateReportTypeDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.REPORT_TYPE_NOT_FOUND));
        }

        String normalizedCode = normalizeCode(request.getCode());
        PlateReportType otherWithCode = plateReportTypeDao.findByCode(normalizedCode).orElse(null);
        if (otherWithCode != null && !otherWithCode.getId().equals(id)) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.REPORT_TYPE_ALREADY_EXISTS));
        }

        applyFields(existing, request.getLabel(), request.getDescription(), request.getIconKey(), request.getSeverity(), request.getColorHex(), request.getWeight(), request.getSortOrder());
        existing.setCode(normalizedCode);
        existing.setUpdatedAt(LocalDateTime.now());

        PlateReportType updated = plateReportTypeDao.save(existing);
        return new SuccessDataResult<>(toAdminDto(updated), messageService.getMessage(Messages.PLATE_REPORT_TYPE_UPDATED));
    }

    @Override
    @Transactional
    public Result setReportTypeActive(Long id, boolean active) {
        PlateReportType existing = plateReportTypeDao.findById(id).orElse(null);
        if (existing == null) {
            return new ErrorResult(messageService.getMessage(Messages.REPORT_TYPE_NOT_FOUND));
        }

        LocalDateTime now = LocalDateTime.now();
        existing.setActive(active);
        existing.setUpdatedAt(now);
        plateReportTypeDao.save(existing);
        if (!active) {
            plateReportDao.deactivateActiveReportsByTypeId(id, now, now);
        }

        return new SuccessResult(messageService.getMessage(Messages.PLATE_REPORT_TYPE_STATUS_UPDATED));
    }

    private void applyFields(
            PlateReportType type,
            String label,
            String description,
            String iconKey,
            com.mefy.platemate.entities.concrete.PlateReportSeverity severity,
            String colorHex,
            Integer weight,
            Integer sortOrder
    ) {
        type.setLabel(label.trim());
        type.setDescription(description.trim());
        type.setIconKey(iconKey.trim());
        type.setSeverity(severity);
        type.setColorHex(colorHex.trim().toUpperCase(Locale.ROOT));
        type.setWeight(weight);
        type.setSortOrder(sortOrder);
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private PlateReportTypeDto toPublicDto(PlateReportType type) {
        return new PlateReportTypeDto(
                type.getCode(),
                type.getLabel(),
                type.getDescription(),
                type.getIconKey(),
                type.getSeverity(),
                type.getColorHex(),
                type.getWeight(),
                type.getSortOrder()
        );
    }

    private PlateReportTypeAdminDto toAdminDto(PlateReportType type) {
        return new PlateReportTypeAdminDto(
                type.getId(),
                type.getCode(),
                type.getLabel(),
                type.getDescription(),
                type.getIconKey(),
                type.getSeverity(),
                type.getColorHex(),
                type.getWeight(),
                type.getSortOrder(),
                type.isActive(),
                type.getCreatedAt(),
                type.getUpdatedAt()
        );
    }
}
