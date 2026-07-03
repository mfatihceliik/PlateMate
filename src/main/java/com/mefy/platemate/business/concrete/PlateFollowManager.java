package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IAppSettingsService;
import com.mefy.platemate.business.abstracts.INotificationService;
import com.mefy.platemate.business.abstracts.IPlateFollowService;
import com.mefy.platemate.business.abstracts.IPlateSearchService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IPlateFollowDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.AppSettingKey;
import com.mefy.platemate.entities.concrete.NotificationType;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.concrete.PlateFollow;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlateFollowManager implements IPlateFollowService {

    private final IPlateFollowDao plateFollowDao;
    private final IUserDao userDao;
    private final IPlateSearchService plateSearchService;
    private final INotificationService notificationService;
    private final IAppSettingsService appSettingsService;
    private final IMessageService messageService;

    @Override
    @Transactional
    public Result followPlate(Long userId, String plateCode) {
        String normalizedPlate = plateSearchService.normalizePlate(plateCode);

        Result validationResult = BusinessRules.run(plateSearchService.checkIfPlateValid(normalizedPlate));
        if (validationResult != null) return validationResult;

        User user = userDao.findByIdAndActiveTrue(userId).orElse(null);
        if (user == null) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }

        Plate plate = plateSearchService.getOrCreatePlate(normalizedPlate);

        if (plateFollowDao.existsByUserIdAndPlateId(userId, plate.getId())) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_FOLLOW_ALREADY_EXISTS));
        }

        int nonPremiumPlateLimit = appSettingsService.getInt(AppSettingKey.NON_PREMIUM_PLATE_FOLLOW_LIMIT);
        if (!user.isPremiumActive() && plateFollowDao.countByUserId(userId) >= nonPremiumPlateLimit) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_FOLLOW_LIMIT_REACHED, nonPremiumPlateLimit));
        }

        PlateFollow plateFollow = new PlateFollow();
        plateFollow.setUser(user);
        plateFollow.setPlate(plate);
        plateFollowDao.save(plateFollow);

        return new SuccessResult(messageService.getMessage(Messages.PLATE_FOLLOW_SUCCESS));
    }

    @Override
    @Transactional
    public Result unfollowPlate(Long userId, String plateCode) {
        String normalizedPlate = plateSearchService.normalizePlate(plateCode);

        Result validationResult = BusinessRules.run(plateSearchService.checkIfPlateValid(normalizedPlate));
        if (validationResult != null) return validationResult;

        Plate plate = plateSearchService.getOrCreatePlate(normalizedPlate);

        PlateFollow plateFollow = plateFollowDao.findByUserIdAndPlateId(userId, plate.getId()).orElse(null);
        if (plateFollow == null) {
            return new ErrorResult(messageService.getMessage(Messages.PLATE_FOLLOW_NOT_FOUND));
        }

        plateFollowDao.delete(plateFollow);
        return new SuccessResult(messageService.getMessage(Messages.PLATE_UNFOLLOW_SUCCESS));
    }

    @Override
    public void notifyReviewApproved(PlateReview review) {
        if (review == null || review.getPlate() == null || review.getPlate().getId() == null) {
            return;
        }

        Long plateId = review.getPlate().getId();
        Long authorId = review.getUser() == null ? null : review.getUser().getId();
        List<Long> followerIds = plateFollowDao.findFollowerUserIdsByPlateId(plateId);
        if (followerIds.isEmpty()) {
            return;
        }

        String plateCode = review.getPlate().getPlateCode();
        String title = messageService.getMessage(Messages.NOTIFICATION_PLATE_REVIEW_TITLE);
        String content = messageService.getMessage(Messages.NOTIFICATION_PLATE_REVIEW_CONTENT, plateCode);

        for (Long followerId : followerIds) {
            if (followerId == null || followerId.equals(authorId)) {
                continue;
            }
            notificationService.sendNotification(followerId, title, content, NotificationType.PLATE_REVIEW.name());
        }
    }
}
