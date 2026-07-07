package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.business.abstracts.ISubscriptionService;
import com.mefy.platemate.core.utilities.mappers.IMapper;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.UserAdminDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAdminMapper implements IMapper<User, UserAdminDto> {

    @Lazy
    private final ISubscriptionService subscriptionService;

    @Override
    public UserAdminDto entityToDto(User entity) {
        if (entity == null) {
            return null;
        }
        Long roleId = null;
        String roleCode = null;
        if (entity.getRole() != null) {
            roleId = entity.getRole().getCodeId();
            roleCode = entity.getRole().getCodeValue();
        }
        return new UserAdminDto(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                roleId,
                roleCode,
                entity.isPremiumActive(),
                subscriptionService.resolvePremiumUntil(entity.getId()),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getDeletedAt()
        );
    }

    @Override
    public User dtoToEntity(UserAdminDto dto) {
        throw new UnsupportedOperationException("Not supported");
    }
}
