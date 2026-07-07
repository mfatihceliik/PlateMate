package com.mefy.platemate.business.utilities.rules;

import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Shared guard for the "load an active user or fail with USER_NOT_FOUND" pattern that was
 * duplicated across many managers. Returns the loaded {@link User} on success so callers do
 * not have to query again.
 */
@Component
@RequiredArgsConstructor
public class UserRules {

    private final IUserDao userDao;
    private final IMessageService messageService;

    public DataResult<User> resolveActiveUser(Long userId) {
        User user = userId == null ? null : userDao.findByIdAndActiveTrue(userId).orElse(null);
        if (user == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        return new SuccessDataResult<>(user);
    }
}
