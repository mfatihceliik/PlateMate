package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IFollowService;
import com.mefy.platemate.business.abstracts.INotificationService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.business.utilities.rules.RelationshipRules;
import com.mefy.platemate.business.utilities.rules.UserRules;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IFollowDao;
import com.mefy.platemate.entities.concrete.Follow;
import com.mefy.platemate.entities.concrete.NotificationType;
import com.mefy.platemate.entities.concrete.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowManager implements IFollowService {

    private final IFollowDao followDao;
    private final INotificationService notificationService;
    private final IMessageService messageService;
    private final UserRules userRules;
    private final RelationshipRules relationshipRules;

    @Override
    @Transactional
    public Result follow(Long followerId, Long followingId) {
        Result guard = BusinessRules.run(
                () -> relationshipRules.notSelf(followerId, followingId, Messages.FOLLOW_SELF_NOT_ALLOWED),
                () -> relationshipRules.notAlreadyExists(
                        followDao.existsByFollowerIdAndFollowingId(followerId, followingId),
                        Messages.FOLLOW_ALREADY_EXISTS));
        if (guard != null) {
            return guard;
        }

        DataResult<User> followerResult = userRules.resolveActiveUser(followerId);
        if (!followerResult.isSuccess()) {
            return new ErrorResult(followerResult.getMessage());
        }
        DataResult<User> followingResult = userRules.resolveActiveUser(followingId);
        if (!followingResult.isSuccess()) {
            return new ErrorResult(followingResult.getMessage());
        }
        User follower = followerResult.getData();
        User following = followingResult.getData();

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        followDao.save(follow);

        String title = messageService.getMessage(Messages.NOTIFICATION_NEW_FOLLOWER_TITLE);
        String content = messageService.getMessage(Messages.NOTIFICATION_NEW_FOLLOWER_CONTENT, follower.getUsername());
        notificationService.sendNotification(followingId, title, content, NotificationType.NEW_FOLLOWER.name());

        return new SuccessResult(messageService.getMessage(Messages.FOLLOW_SUCCESS));
    }

    @Override
    @Transactional
    public Result unfollow(Long followerId, Long followingId) {
        Follow follow = followDao.findByFollowerIdAndFollowingId(followerId, followingId).orElse(null);
        if (follow == null) {
            return new ErrorResult(messageService.getMessage(Messages.FOLLOW_NOT_FOUND));
        }

        followDao.delete(follow);
        return new SuccessResult(messageService.getMessage(Messages.UNFOLLOW_SUCCESS));
    }

    @Override
    public long countFollowers(Long userId) {
        return followDao.countByFollowingId(userId);
    }

    @Override
    public long countFollowing(Long userId) {
        return followDao.countByFollowerId(userId);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }
        return followDao.existsByFollowerIdAndFollowingId(followerId, followingId);
    }
}
