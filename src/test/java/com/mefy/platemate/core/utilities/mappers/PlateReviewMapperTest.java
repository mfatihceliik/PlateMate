package com.mefy.platemate.core.utilities.mappers;

import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.PlateReviewDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PlateReviewMapperTest {

    private final PlateReviewMapper mapper = new PlateReviewMapper();

    @Test
    void entityToDtoMapsReviewStatusFromEntityStatus() {
        Plate plate = new Plate();
        plate.setPlateCode("34ABC123");

        User user = new User();
        user.setId(17L);
        user.setUsername("fatih");

        PlateReview review = new PlateReview();
        review.setId(9L);
        review.setPlate(plate);
        review.setUser(user);
        review.setRating(4);
        review.setComment("yorum");
        review.setStatus(PlateReviewStatus.REJECTED);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        PlateReviewDto dto = mapper.entityToDto(review);

        assertNotNull(dto);
        assertEquals(PlateReviewStatus.REJECTED, dto.getReviewStatus());
        assertEquals("34ABC123", dto.getPlateCode());
        assertEquals(17L, dto.getUserId());
    }
}
