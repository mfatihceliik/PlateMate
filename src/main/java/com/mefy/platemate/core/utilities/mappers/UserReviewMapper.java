package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.entities.concrete.UserReview;
import com.mefy.platemate.entities.dto.UserReviewDto;
import org.springframework.stereotype.Component;

@Component
public class UserReviewMapper implements ModelMapperService<UserReview, UserReviewDto> {
    @Override
    public UserReviewDto entityToDto(UserReview entity) {
        if (entity == null) return null;
        UserReviewDto dto = new UserReviewDto();
        dto.setId(entity.getId());
        dto.setRating(entity.getRating());
        dto.setComment(entity.getComment());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getReviewer() != null) {
            dto.setReviewerUsername(entity.getReviewer().getUsername());
        }
        return dto;
    }

    @Override
    public UserReview dtoToEntity(UserReviewDto dto) { return null; }
}
