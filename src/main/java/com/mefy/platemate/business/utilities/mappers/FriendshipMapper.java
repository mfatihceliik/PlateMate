package com.mefy.platemate.business.utilities.mappers;

import com.mefy.platemate.core.utilities.mappers.IMapper;

import com.mefy.platemate.business.utilities.i18n.LocalizedEnumService;
import com.mefy.platemate.entities.concrete.Friendship;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.FriendshipDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FriendshipMapper implements IMapper<Friendship, FriendshipDto> {

    // Field-injected cross-cutting i18n helper: optional so no-arg unit constructions stay valid.
    @Autowired(required = false)
    private LocalizedEnumService localizedEnumService;

    /**
     * Converts Entity to DTO.
     * When used without currentUserId parameter, it views from the requester's perspective.
     * In actual use, the overloaded method should be preferred.
     */
    @Override
    public FriendshipDto entityToDto(Friendship entity) {
        if (entity == null) return null;
        // By default, view from requester perspective
        return entityToDto(entity, entity.getRequester().getId());
    }

    /**
     * Determines the other party based on the current user.
     * If I am the requester -> the other party is addressee
     * If I am the addressee -> the other party is requester
     */
    public FriendshipDto entityToDto(Friendship entity, Long currentUserId) {
        if (entity == null) return null;

        FriendshipDto dto = new FriendshipDto();
        dto.setId(entity.getId());
        dto.setStatusId(entity.getStatusId());
        dto.setStatusCode(entity.getStatusCode());
        if (localizedEnumService != null) {
            dto.setStatusLabel(localizedEnumService.label("friendship_status", entity.getStatusCode()));
        }
        dto.setRequestedAt(entity.getCreatedAt());
        dto.setRespondedAt(entity.getRespondedAt());

        // Determine the other party
        User friend = entity.getRequester().getId().equals(currentUserId)
                ? entity.getAddressee()
                : entity.getRequester();

        dto.setFriendUserId(friend.getId());
        dto.setFriendUsername(friend.getUsername());

        return dto;
    }

    @Override
    public Friendship dtoToEntity(FriendshipDto dto) {
        return null;
    }
}


