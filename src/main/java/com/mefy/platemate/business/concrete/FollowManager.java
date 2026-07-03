package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IFollowService;
import com.mefy.platemate.business.abstracts.INotificationService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IFollowDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
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
    private final IUserDao userDao;
    private final INotificationService notificationService;
    private final IMessageService messageService;

    @Override
    @Transactional
    public Result follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            return new ErrorResult(messageService.getMessage(Messages.FOLLOW_SELF_NOT_ALLOWED));
        }

        if (followDao.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            return new ErrorResult(messageService.getMessage(Messages.FOLLOW_ALREADY_EXISTS));
        }

        User follower = userDao.findByIdAndActiveTrue(followerId).orElse(null);
        if (follower == null) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }

        User following = userDao.findByIdAndActiveTrue(followingId).orElse(null);
        if (following == null) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }

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
