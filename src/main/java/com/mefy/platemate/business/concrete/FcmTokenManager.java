package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IFcmTokenService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.dataAccess.abstracts.IFcmTokenDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserFcmToken;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FcmTokenManager implements IFcmTokenService {

    private final IFcmTokenDao fcmTokenDao;
    private final IUserDao userDao;
    private final IMessageService messageService;

    @Override
    @Transactional
    public Result registerToken(Long userId, String token, String deviceId) {
        User user = userDao.findById(userId).orElse(null);
        if (user == null) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }

        var existingToken = fcmTokenDao.findByToken(token);

        if (existingToken.isPresent()) {
            UserFcmToken fcmToken = existingToken.get();
            // Eğer token başka bir kullanıcıya geçtiyse güncelle
            if (!fcmToken.getUser().getId().equals(userId)) {
                fcmToken.setUser(user);
            }
            fcmToken.setDeviceId(deviceId);
            fcmToken.setLastUpdatedAt(LocalDateTime.now());
            fcmTokenDao.save(fcmToken);
        } else {
            UserFcmToken fcmToken = new UserFcmToken();
            fcmToken.setUser(user);
            fcmToken.setToken(token);
            fcmToken.setDeviceId(deviceId);
            fcmTokenDao.save(fcmToken);
        }

        return new SuccessResult();
    }

    @Override
    @Transactional
    public Result unregisterToken(Long currentUserId, String token) {
        if (!isTokenOwnedByUser(token, currentUserId)) {
            return new ErrorResult(messageService.getMessage("auth.unauthorized"));
        }
        fcmTokenDao.deleteByToken(token);
        return new SuccessResult();
    }

    private boolean isTokenOwnedByUser(String token, Long userId) {
        return fcmTokenDao.findByTokenAndUserId(token, userId).isPresent();
    }
}

