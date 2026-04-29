package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IFcmTokenService;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
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

    @Override
    @Transactional
    public Result registerToken(Long userId, String token, String deviceId) {
        var existingToken = fcmTokenDao.findByToken(token);
        
        if (existingToken.isPresent()) {
            UserFcmToken fcmToken = existingToken.get();
            // Eğer token başka bir kullanıcıya geçtiyse güncelle
            if (!fcmToken.getUser().getId().equals(userId)) {
                User user = userDao.findById(userId).orElseThrow();
                fcmToken.setUser(user);
            }
            fcmToken.setDeviceId(deviceId);
            fcmToken.setLastUpdatedAt(LocalDateTime.now());
            fcmTokenDao.save(fcmToken);
        } else {
            User user = userDao.findById(userId).orElseThrow();
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
    public Result unregisterToken(String token) {
        fcmTokenDao.deleteByToken(token);
        return new SuccessResult();
    }
}
