package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IFriendshipService;
import com.mefy.platemate.business.abstracts.INotificationService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.business.utilities.mappers.FriendshipMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IFriendshipDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.Friendship;
import com.mefy.platemate.entities.concrete.FriendshipRequestStatusCodes;
import com.mefy.platemate.entities.concrete.NotificationType;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.FriendshipDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipManager implements IFriendshipService {

    private final IFriendshipDao friendshipDao;
    private final IUserDao userDao;
    private final FriendshipMapper friendshipMapper;
    private final INotificationService notificationService;
    private final IMessageService messageService;

    @Override
    @Transactional
    public Result sendRequest(Long requesterId, Long addresseeId) {
        Result result = BusinessRules.run(
                checkIfSelfRequest(requesterId, addresseeId),
                checkIfAlreadyExists(requesterId, addresseeId)
        );
        if (result != null) return result;

        User requester = userDao.findById(requesterId).orElse(null);
        User addressee = userDao.findById(addresseeId).orElse(null);

        Result usersResult = BusinessRules.run(checkIfUsersExist(requester, addressee));
        if (usersResult != null) return usersResult;

        Friendship friendship = new Friendship();
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendship.setStatusId(FriendshipRequestStatusCodes.REQUESTED_ID);
        friendship.setRespondedAt(null);
        friendshipDao.save(friendship);

        String title = messageService.getMessage(Messages.NOTIFICATION_FRIEND_REQUEST_TITLE);
        String content = requester.getUsername() + " " + messageService.getMessage(Messages.NOTIFICATION_FRIEND_REQUEST_CONTENT);
        notificationService.sendNotification(addresseeId, title, content, NotificationType.FRIEND_REQUEST.name());

        return new SuccessResult(messageService.getMessage(Messages.FRIENDSHIP_REQUEST_SENT));
    }

    @Override
    @Transactional
    public Result acceptRequest(Long friendshipId, Long currentUserId) {
        Friendship friendship = friendshipDao.findById(friendshipId).orElse(null);

        Result result = BusinessRules.run(
                checkIfFriendshipExists(friendship),
                checkIfUserAuthorized(friendship, currentUserId, "friendship.accept.unauthorized"),
                checkIfFriendshipIsPending(friendship)
        );
        if (result != null) return result;

        friendship.setStatusId(FriendshipRequestStatusCodes.ACCEPTED_ID);
        friendship.setRespondedAt(LocalDateTime.now());
        friendshipDao.save(friendship);
        return new SuccessResult(messageService.getMessage(Messages.FRIENDSHIP_ACCEPTED));
    }

    @Override
    @Transactional
    public Result rejectRequest(Long friendshipId, Long currentUserId) {
        Friendship friendship = friendshipDao.findById(friendshipId).orElse(null);

        Result result = BusinessRules.run(
                checkIfFriendshipExists(friendship),
                checkIfUserAuthorized(friendship, currentUserId, "friendship.reject.unauthorized"),
                checkIfFriendshipIsPending(friendship)
        );
        if (result != null) return result;

        friendship.setStatusId(FriendshipRequestStatusCodes.REJECTED_ID);
        friendship.setRespondedAt(LocalDateTime.now());
        friendshipDao.save(friendship);
        return new SuccessResult(messageService.getMessage(Messages.FRIENDSHIP_REJECTED));
    }

    @Override
    @Transactional
    public Result removeFriend(Long friendshipId, Long currentUserId) {
        Friendship friendship = friendshipDao.findById(friendshipId).orElse(null);

        Result result = BusinessRules.run(
                checkIfFriendshipExists(friendship),
                checkIfUserIsParticipant(friendship, currentUserId)
        );
        if (result != null) return result;

        friendship.setStatusId(FriendshipRequestStatusCodes.REMOVED_ID);
        friendship.setRespondedAt(LocalDateTime.now());
        friendshipDao.save(friendship);
        return new SuccessResult(messageService.getMessage(Messages.FRIENDSHIP_REMOVED));
    }

    @Override
    public DataResult<List<FriendshipDto>> getFriends(Long userId) {
        List<Friendship> friendships = friendshipDao.findByUserIdAndStatusId(userId, FriendshipRequestStatusCodes.ACCEPTED_ID);
        List<FriendshipDto> dtos = friendships.stream()
                .map(f -> friendshipMapper.entityToDto(f, userId))
                .collect(Collectors.toList());
        return new SuccessDataResult<>(dtos, messageService.getMessage(Messages.FRIENDS_LISTED));
    }

    @Override
    public DataResult<List<FriendshipDto>> getPendingRequests(Long userId) {
        List<Friendship> pending = friendshipDao.findByAddresseeIdAndStatusId(userId, FriendshipRequestStatusCodes.REQUESTED_ID);
        List<FriendshipDto> dtos = pending.stream()
                .map(f -> friendshipMapper.entityToDto(f, userId))
                .collect(Collectors.toList());
        return new SuccessDataResult<>(dtos, messageService.getMessage(Messages.PENDING_REQUESTS_LISTED));
    }

    @Override
    public DataResult<List<FriendshipDto>> getSentPendingRequests(Long userId) {
        List<Friendship> sent = friendshipDao.findByRequesterIdAndStatusId(userId, FriendshipRequestStatusCodes.REQUESTED_ID);
        List<FriendshipDto> dtos = sent.stream()
                .map(f -> friendshipMapper.entityToDto(f, userId))
                .collect(Collectors.toList());
        return new SuccessDataResult<>(dtos, messageService.getMessage(Messages.SENT_REQUESTS_LISTED));
    }

    @Override
    public boolean areFriends(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null || userId1.equals(userId2)) {
            return false;
        }
        return friendshipDao.countActiveBetweenUsers(
                userId1,
                userId2,
                java.util.Set.of(FriendshipRequestStatusCodes.ACCEPTED_ID)
        ) > 0;
    }

    private Result checkIfSelfRequest(Long requesterId, Long addresseeId) {
        if (requesterId.equals(addresseeId)) {
            return new ErrorResult(messageService.getMessage(Messages.FRIENDSHIP_SELF_REQUEST));
        }
        return new SuccessResult();
    }

    private Result checkIfAlreadyExists(Long requesterId, Long addresseeId) {
        long activeCount = friendshipDao.countActiveBetweenUsers(
                requesterId,
                addresseeId,
                FriendshipRequestStatusCodes.ACTIVE_IDS
        );
        if (activeCount > 0) {
            return new ErrorResult(messageService.getMessage(Messages.FRIENDSHIP_ALREADY_EXISTS));
        }
        return new SuccessResult();
    }

    private Result checkIfUsersExist(User requester, User addressee) {
        if (requester == null || addressee == null) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkIfFriendshipExists(Friendship friendship) {
        if (friendship == null) {
            return new ErrorResult(messageService.getMessage(Messages.FRIENDSHIP_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkIfUserAuthorized(Friendship friendship, Long currentUserId, String unauthorizedMessageKey) {
        if (friendship == null) return new SuccessResult();
        if (!friendship.getAddressee().getId().equals(currentUserId)) {
            return new ErrorResult(messageService.getMessage(unauthorizedMessageKey));
        }
        return new SuccessResult();
    }

    private Result checkIfFriendshipIsPending(Friendship friendship) {
        if (friendship == null) return new SuccessResult();
        if (!FriendshipRequestStatusCodes.REQUESTED_ID.equals(friendship.getStatusId())) {
            return new ErrorResult(messageService.getMessage(Messages.FRIENDSHIP_ALREADY_ANSWERED));
        }
        return new SuccessResult();
    }

    private Result checkIfUserIsParticipant(Friendship friendship, Long currentUserId) {
        if (friendship == null) return new SuccessResult();
        boolean isParticipant = friendship.getRequester().getId().equals(currentUserId)
                || friendship.getAddressee().getId().equals(currentUserId);
        if (!isParticipant) {
            return new ErrorResult(messageService.getMessage(Messages.FRIENDSHIP_REMOVE_UNAUTHORIZED));
        }
        return new SuccessResult();
    }
}

