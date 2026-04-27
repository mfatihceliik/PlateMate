package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IUserService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.mappers.UserMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.*;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManager implements IUserService {

    private final IUserDao userDao;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final IMessageService messageService;

    @Override
    public DataResult<User> add(User user) {
        Result result = BusinessRules.run(
                checkIfUsernameExists(user.getUsername()),
                checkIfEmailExists(user.getEmail())
        );

        if (result != null) {
            return new ErrorDataResult<>(result.getMessage());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        user.setProfile(profile);

        userDao.save(user);
        return new SuccessDataResult<>(user, messageService.getMessage(Messages.USER_ADDED));
    }

    @Override
    public DataResult<List<UserDto>> getAll() {
        List<User> users = userDao.findAll();
        List<UserDto> userDtos = users.stream()
                .map(userMapper::entityToDto)
                .collect(Collectors.toList());

        return new SuccessDataResult<>(userDtos, messageService.getMessage(Messages.USERS_LISTED));
    }

    @Override
    public DataResult<UserDto> getById(Long id) {
        User user = userDao.findById(id).orElse(null);
        if (user == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        return new SuccessDataResult<>(userMapper.entityToDto(user), messageService.getMessage(Messages.USER_FOUND));
    }

    @Override
    public Result delete(Long id) {
        Result result = BusinessRules.run(checkIfUserExistsById(id));
        if (result != null) return result;

        userDao.deleteById(id);
        return new SuccessResult(messageService.getMessage(Messages.USER_DELETED));
    }

    @Override
    public DataResult<UserDto> getByUsername(String username) {
        User user = userDao.findByUsername(username).orElse(null);
        if (user == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        return new SuccessDataResult<>(userMapper.entityToDto(user), messageService.getMessage(Messages.USER_FOUND));
    }

    @Override
    public DataResult<User> getByUsernameOrEmailForAuth(String identifier) {
        User user = userDao.findByUsernameOrEmail(identifier, identifier).orElse(null);
        if (user == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        return new SuccessDataResult<>(user, messageService.getMessage(Messages.USER_FOUND));
    }

    // --- BUSINESS RULES ---

    private Result checkIfUsernameExists(String username) {
        if (userDao.existsByUsername(username)) {
            return new ErrorResult(messageService.getMessage(Messages.USER_USERNAME_EXISTS));
        }
        return new SuccessResult();
    }

    private Result checkIfUserExistsById(Long id) {
        if (!userDao.existsById(id)) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkIfEmailExists(String email) {
        if (email != null && userDao.existsByEmail(email)) {
            return new ErrorResult(messageService.getMessage(Messages.USER_EMAIL_EXISTS));
        }
        return new SuccessResult();
    }
}
