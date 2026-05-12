package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.plate.abstracts.IPlateValidator;
import com.mefy.platemate.business.utilities.plate.concrete.TrPlateCityResolver;
import com.mefy.platemate.core.utilities.mappers.PlateMapper;
import com.mefy.platemate.core.utilities.mappers.PlateReviewMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.dataAccess.abstracts.ICityDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateDao;
import com.mefy.platemate.dataAccess.abstracts.IPlateReviewDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.PlateDto;
import com.mefy.platemate.entities.dto.request.AddPlateReviewRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlateManagerTest {

    @Mock
    private IPlateDao plateDao;
    @Mock
    private IPlateReviewDao plateReviewDao;
    @Mock
    private IUserDao userDao;
    @Mock
    private ICityDao cityDao;
    @Mock
    private IPlateValidator plateValidator;
    @Mock
    private IMessageService messageService;

    private PlateManager plateManager;

    @BeforeEach
    void setUp() {
        plateManager = new PlateManager(
                plateDao,
                plateReviewDao,
                userDao,
                cityDao,
                new PlateMapper(),
                new PlateReviewMapper(),
                plateValidator,
                new TrPlateCityResolver(),
                messageService
        );
    }

    @Test
    void searchByPlateCodeCreatesPlateWhenNotFound() {
        Plate saved = new Plate();
        saved.setId(1L);
        saved.setPlateCode("34ABC123");
        saved.setRatingAverage(0.0);
        saved.setReviewCount(0);
        saved.setTotalRatingSum(0L);

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.empty());
        when(plateDao.save(any(Plate.class))).thenReturn(saved);
        when(messageService.getMessage(Messages.PLATE_FOUND)).thenReturn("plate-found");

        DataResult<PlateDto> result = plateManager.searchByPlateCode("34 ABC 123");

        assertTrue(result.isSuccess());
        assertEquals("plate-found", result.getMessage());
        assertEquals("34ABC123", result.getData().getPlateCode());
        assertEquals(0.0, result.getData().getRatingAverage());
        assertEquals(0, result.getData().getReviewCount());
        assertEquals(0L, result.getData().getTotalRatingSum());
        assertEquals("Istanbul", result.getData().getCityName());
    }

    @Test
    void searchByPlateCodeReturnsErrorForInvalidPlate() {
        when(plateValidator.isValid("XX")).thenReturn(false);
        when(messageService.getMessage(Messages.PLATE_INVALID)).thenReturn("invalid-plate");

        DataResult<PlateDto> result = plateManager.searchByPlateCode("XX");

        assertTrue(!result.isSuccess());
        assertEquals("invalid-plate", result.getMessage());
    }

    @Test
    void addReviewUpdatesAggregateValuesFromPlateReviews() {
        User user = new User();
        user.setId(99L);

        Plate plate = new Plate();
        plate.setId(10L);
        plate.setPlateCode("34ABC123");

        when(plateValidator.isValid("34ABC123")).thenReturn(true);
        when(userDao.findById(99L)).thenReturn(Optional.of(user));
        when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));
        when(plateReviewDao.findByPlateIdAndUserId(10L, 99L)).thenReturn(Optional.empty());
        when(plateReviewDao.countByPlateId(10L)).thenReturn(1L);
        when(plateReviewDao.sumRatingByPlateId(10L)).thenReturn(5L);
        when(messageService.getMessage(Messages.REVIEW_ADDED)).thenReturn("review-added");

        Result result = plateManager.addReview("34 ABC 123", 99L, new AddPlateReviewRequest(5, "iyi"));

        assertTrue(result.isSuccess());
        assertEquals("review-added", result.getMessage());
        assertEquals(1, plate.getReviewCount());
        assertEquals(5L, plate.getTotalRatingSum());
        assertEquals(5.0, plate.getRatingAverage());
        verify(plateDao).save(plate);
    }
}
