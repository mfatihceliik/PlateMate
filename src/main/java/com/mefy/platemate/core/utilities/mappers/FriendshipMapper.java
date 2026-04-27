package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.entities.concrete.Friendship;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.FriendshipDto;
import org.springframework.stereotype.Component;

@Component
public class FriendshipMapper implements ModelMapperService<Friendship, FriendshipDto> {

    /**
     * Entity'yi DTO'ya çevirir.
     * currentUserId parametresi olmadan kullanıldığında requester perspektifinden bakar.
     * Gerçek kullanımda overload metodu tercih edilmeli.
     */
    @Override
    public FriendshipDto entityToDto(Friendship entity) {
        if (entity == null) return null;
        // Varsayılan olarak requester perspektifinden bakar
        return entityToDto(entity, entity.getRequester().getId());
    }

    /**
     * Current user'a göre karşı tarafı belirler.
     * Eğer ben requester isem → karşı taraf addressee
     * Eğer ben addressee isem → karşı taraf requester
     */
    public FriendshipDto entityToDto(Friendship entity, Long currentUserId) {
        if (entity == null) return null;

        FriendshipDto dto = new FriendshipDto();
        dto.setId(entity.getId());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());

        // Karşı tarafı belirle
        User friend = entity.getRequester().getId().equals(currentUserId)
                ? entity.getAddressee()
                : entity.getRequester();

        dto.setFriendUserId(friend.getId());
        dto.setFriendUsername(friend.getUsername());

        if (friend.getProfile() != null) {
            dto.setFriendFirstName(friend.getProfile().getFirstName());
            dto.setFriendLastName(friend.getProfile().getLastName());
        }

        return dto;
    }

    @Override
    public Friendship dtoToEntity(FriendshipDto dto) {
        return null;
    }
}
