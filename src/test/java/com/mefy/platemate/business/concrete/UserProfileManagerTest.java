package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.mappers.PlateReviewMapper;
import com.mefy.platemate.core.utilities.mappers.SocialMediaLinkMapper;
import com.mefy.platemate.core.utilities.mappers.UserProfileMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IUserProfileDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.dto.UserProfileDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileManagerTest {

    @Mock
    private IUserProfileDao userProfileDao;
    @Mock
    private IPlateReviewDao plateReviewDao;
    @Mock
    private IMessageService messageService;

    private UserProfileManager userProfileManager;

    @BeforeEach
    void setUp() {
        userProfileManager = new UserProfileManager(
                userProfileDao,
                plateReviewDao,
                new UserProfileMapper(new SocialMediaLinkMapper()),
                new PlateReviewMapper(),
                messageService
        );
    }

    @Test
    void getByUserIdReturnsPlateReviewsAsPagedData() {
        User user = new User();
        user.setId(3L);
        user.setUsername("fatih");

        UserProfile profile = new UserProfile();
        profile.setId(3L);
        profile.setUser(user);

        Plate plate = new Plate();
        plate.setId(8L);
        plate.setPlateCode("34ABC123");

        PlateReview review = new PlateReview();
        review.setId(11L);
        review.setPlate(plate);
        review.setUser(user);
        review.setRating(4);
        review.setComment("iyi surucu");
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        when(userProfileDao.findById(3L)).thenReturn(Optional.of(profile));
        when(plateReviewDao.findByUserId(eq(3L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(review), PageRequest.of(0, 20), 1));
        when(plateReviewDao.countByUserId(3L)).thenReturn(1L);
        when(plateReviewDao.sumRatingByUserId(3L)).thenReturn(4L);
        when(messageService.getMessage(Messages.PROFILE_FOUND)).thenReturn("profile-found");

        DataResult<UserProfileDto> result = userProfileManager.getByUserId(3L, PaginationRequest.of(0, 20));

        assertTrue(result.isSuccess());
        assertEquals("profile-found", result.getMessage());
        assertEquals("fatih", result.getData().getUsername());
        assertEquals(1, result.getData().getPlateReviews().getItems().size());
        assertEquals(0, result.getData().getPlateReviews().getMeta().getPage());
        assertEquals(20, result.getData().getPlateReviews().getMeta().getSize());
        assertEquals(1L, result.getData().getPlateReviews().getMeta().getTotalElements());
        assertEquals(4L, result.getData().getTotalRatingSum());
        assertEquals(1, result.getData().getReviewCount());
        assertEquals(4.0, result.getData().getDriverRating());
    }
}
