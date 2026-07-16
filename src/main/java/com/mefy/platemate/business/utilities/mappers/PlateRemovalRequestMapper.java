package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.business.utilities.i18n.LocalizedEnumService;
import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.PlateRemovalRequest;
import com.mefy.platemate.entities.dto.PlateRemovalRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlateRemovalRequestMapper implements IMapper<PlateRemovalRequest, PlateRemovalRequestDto> {

    private final LocalizedEnumService localizedEnumService;

    @Override
    public PlateRemovalRequestDto entityToDto(PlateRemovalRequest entity) {
        if (entity == null) {
            return null;
        }
        String plateCode = entity.getPlate() != null ? entity.getPlate().getPlateCode() : null;
        Long plateId = entity.getPlate() != null ? entity.getPlate().getId() : null;
        PlateRemovalRequestDto dto = new PlateRemovalRequestDto();
        dto.setId(entity.getId());
        dto.setPlateId(plateId);
        dto.setPlateCode(plateCode);
        dto.setRequesterUserId(entity.getRequesterUserId());
        dto.setRequesterEmail(entity.getRequesterEmail());
        dto.setRequesterUsername(entity.getRequesterUsername());
        dto.setReasonCode(entity.getReasonCode());
        dto.setDescription(entity.getDescription());
        dto.setStatusId(entity.getStatusId());
        dto.setStatusCode(entity.getStatusCode());
        dto.setAdminNote(entity.getAdminNote());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setReviewedAt(entity.getReviewedAt());
        dto.setReviewedBy(entity.getReviewedBy());
        if (localizedEnumService != null) {
            dto.setReasonLabel(localizedEnumService.label("plate_removal_request_reason", entity.getReasonCode()));
            dto.setStatusLabel(localizedEnumService.label("removal_status", entity.getStatusCode()));
        }
        return dto;
    }

    @Override
    public PlateRemovalRequest dtoToEntity(PlateRemovalRequestDto dto) {
        throw new UnsupportedOperationException("Mapping from PlateRemovalRequestDto to Entity is not supported");
    }
}
