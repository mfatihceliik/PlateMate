package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IAdminAccessService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserRoleCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAccessManager implements IAdminAccessService {

    private final IUserDao userDao;
    private final IMessageService messageService;

    @Override
    public Result checkAdmin(Long userId) {
        User user = userDao.findById(userId).orElse(null);
        if (user == null) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        if (!user.hasRole(UserRoleCode.ADMIN)) {
            return new ErrorResult(messageService.getMessage("auth.unauthorized"));
        }
        return new SuccessResult();
    }
}
