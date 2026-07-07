package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IUserBlockService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.business.utilities.rules.RelationshipRules;
import com.mefy.platemate.business.utilities.rules.UserRules;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IUserBlockDao;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserBlock;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserBlockManager implements IUserBlockService {

    private final IUserBlockDao userBlockDao;
    private final IMessageService messageService;
    private final UserRules userRules;
    private final RelationshipRules relationshipRules;

    @Override
    @Transactional
    public Result blockUser(Long blockerId, Long blockedId) {
        Result guard = BusinessRules.run(
                () -> relationshipRules.notSelf(blockerId, blockedId, Messages.USER_BLOCK_SELF_NOT_ALLOWED),
                () -> relationshipRules.notAlreadyExists(
                        userBlockDao.existsByBlockerIdAndBlockedId(blockerId, blockedId),
                        Messages.USER_BLOCK_ALREADY_EXISTS));
        if (guard != null) {
            return guard;
        }

        DataResult<User> blockerResult = userRules.resolveActiveUser(blockerId);
        if (!blockerResult.isSuccess()) {
            return new ErrorResult(blockerResult.getMessage());
        }
        DataResult<User> blockedResult = userRules.resolveActiveUser(blockedId);
        if (!blockedResult.isSuccess()) {
            return new ErrorResult(blockedResult.getMessage());
        }
        User blocker = blockerResult.getData();
        User blocked = blockedResult.getData();

        UserBlock userBlock = new UserBlock();
        userBlock.setBlocker(blocker);
        userBlock.setBlocked(blocked);
        userBlockDao.save(userBlock);

        return new SuccessResult(messageService.getMessage(Messages.USER_BLOCK_SUCCESS));
    }

    @Override
    @Transactional
    public Result unblockUser(Long blockerId, Long blockedId) {
        UserBlock userBlock = userBlockDao.findByBlockerIdAndBlockedId(blockerId, blockedId).orElse(null);
        if (userBlock == null) {
            return new ErrorResult(messageService.getMessage(Messages.USER_BLOCK_NOT_FOUND));
        }

        userBlockDao.delete(userBlock);
        return new SuccessResult(messageService.getMessage(Messages.USER_UNBLOCK_SUCCESS));
    }

    @Override
    public boolean isBlockedEitherWay(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null) {
            return false;
        }
        return userBlockDao.existsBlockBetween(userId1, userId2);
    }
}
