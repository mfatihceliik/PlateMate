package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.business.utilities.i18n.LocalizedEnumService;
import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.UserSubscription;
import com.mefy.platemate.entities.dto.UserSubscriptionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionMapper implements IMapper<UserSubscription, UserSubscriptionDto> {

    private final LocalizedEnumService localizedEnumService;

    @Override
    public UserSubscriptionDto entityToDto(UserSubscription entity) {
        if (entity == null) {
            return null;
        }
        UserSubscriptionDto dto = new UserSubscriptionDto();
        dto.setId(entity.getId());
        dto.setPurchasedDays(entity.getPurchasedDays());
        dto.setStatusId(entity.getStatusId());
        dto.setStatusCode(entity.getStatusCode());
        if (localizedEnumService != null) {
            dto.setStatusLabel(localizedEnumService.label("subscription_status", entity.getStatusCode()));
        }
        dto.setStartedAt(entity.getStartedAt());
        dto.setExpiresAt(entity.getExpiresAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    @Override
    public UserSubscription dtoToEntity(UserSubscriptionDto dto) {
        throw new UnsupportedOperationException("Not supported");
    }
}
