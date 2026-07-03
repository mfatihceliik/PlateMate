package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IUserService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.mappers.UserMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorDataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessDataResult;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IUserRoleDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.dataAccess.abstracts.IUserSubscriptionDao;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserProfile;
import com.mefy.platemate.entities.concrete.UserRole;
import com.mefy.platemate.entities.concrete.UserRoleCode;
import com.mefy.platemate.entities.concrete.UserSubscriptionStatus;
import com.mefy.platemate.entities.dto.UserAdminDto;
import com.mefy.platemate.entities.dto.UserDto;
import com.mefy.platemate.entities.dto.request.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManager implements IUserService {

    private final IUserDao userDao;
    private final IUserRoleDao userRoleDao;
    private final IUserSubscriptionDao userSubscriptionDao;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final IMessageService messageService;

    @Override
    @Transactional
    public DataResult<User> add(User user) {
        Result result = BusinessRules.run(
                checkIfUsernameExists(user.getUsername()),
                checkIfEmailExists(user.getEmail())
        );

        if (result != null) {
            return new ErrorDataResult<>(result.getMessage());
        }

        UserRole normalRole = userRoleDao.findByCode(UserRoleCode.NORMAL).orElse(null);
        if (normalRole == null) {
            return new ErrorDataResult<>(messageService.getMessage(Messages.USER_ROLE_NOT_FOUND));
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(normalRole);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        user.setProfile(profile);

        userDao.save(user);
        return new SuccessDataResult<>(user, messageService.getMessage(Messages.USER_ADDED));
    }

    @Override
    @Transactional
    public DataResult<User> register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        return add(user);
    }

    @Override
    public DataResult<List<UserDto>> getAll() {
        List<User> users = userDao.findAll().stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
        List<UserDto> userDtos = users.stream()
                .map(this::toUserDtoWithComputedPremium)
                .collect(Collectors.toList());

        return new SuccessDataResult<>(userDtos, messageService.getMessage(Messages.USERS_LISTED));
    }

    @Override
    public DataResult<List<UserAdminDto>> getAllForAdmin() {
        List<UserAdminDto> userDtos = userDao.findAll().stream()
                .map(this::toAdminDto)
                .collect(Collectors.toList());
        return new SuccessDataResult<>(userDtos, messageService.getMessage(Messages.USERS_LISTED));
    }

    @Override
    public DataResult<UserDto> getById(Long id) {
        User user = userDao.findByIdAndActiveTrue(id).orElse(null);
        
        Result result = BusinessRules.run(checkIfUserExists(user));
        if (result != null) {
            return new ErrorDataResult<>(result.getMessage());
        }
        
        return new SuccessDataResult<>(toUserDtoWithComputedPremium(user), messageService.getMessage(Messages.USER_FOUND));
    }

    @Override
    public DataResult<UserAdminDto> getByIdForAdmin(Long id) {
        User user = userDao.findById(id).orElse(null);

        Result result = BusinessRules.run(checkIfUserExists(user));
        if (result != null) {
            return new ErrorDataResult<>(result.getMessage());
        }

        return new SuccessDataResult<>(toAdminDto(user), messageService.getMessage(Messages.USER_FOUND));
    }

    @Override
    @Transactional
    public Result update(Long id, com.mefy.platemate.entities.dto.request.UpdateUserRequest request) {
        User existingUser = userDao.findByIdAndActiveTrue(id).orElse(null);
        
        Result result = BusinessRules.run(
                checkIfUserExists(existingUser),
                checkEmailUpdate(request.getEmail(), existingUser)
        );
        if (result != null) return result;

        // Update if email changed
        if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
            existingUser.setEmail(request.getEmail());
        }

        // Update password with hash if changed
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userDao.save(existingUser);
        return new SuccessResult(messageService.getMessage(Messages.USER_UPDATED));
    }

    @Override
    @Transactional
    public Result delete(Long id) {
        User user = userDao.findByIdAndActiveTrue(id).orElse(null);
        Result result = BusinessRules.run(checkIfUserExists(user));
        if (result != null) return result;

        user.setActive(false);
        user.setDeletedAt(LocalDateTime.now());
        userDao.save(user);
        return new SuccessResult(messageService.getMessage(Messages.USER_DELETED));
    }

    @Override
    public DataResult<UserDto> getByUsername(String username) {
        User user = userDao.findByUsernameAndActiveTrue(username).orElse(null);
        
        Result result = BusinessRules.run(checkIfUserExists(user));
        if (result != null) {
            return new ErrorDataResult<>(result.getMessage());
        }
        
        return new SuccessDataResult<>(toUserDtoWithComputedPremium(user), messageService.getMessage(Messages.USER_FOUND));
    }

    @Override
    public DataResult<UserAdminDto> getByUsernameForAdmin(String username) {
        User user = userDao.findByUsername(username).orElse(null);

        Result result = BusinessRules.run(checkIfUserExists(user));
        if (result != null) {
            return new ErrorDataResult<>(result.getMessage());
        }

        return new SuccessDataResult<>(toAdminDto(user), messageService.getMessage(Messages.USER_FOUND));
    }

    @Override
    public DataResult<User> getByIdForAuth(Long id) {
        User user = userDao.findByIdAndActiveTrue(id).orElse(null);

        Result result = BusinessRules.run(checkIfUserExists(user));
        if (result != null) {
            return new ErrorDataResult<>(result.getMessage());
        }

        return new SuccessDataResult<>(user, messageService.getMessage(Messages.USER_FOUND));
    }

    @Override
    public DataResult<User> getByUsernameOrEmailForAuth(String identifier) {
        User user = userDao.findByUsernameOrEmailAndActiveTrue(identifier, identifier).orElse(null);
        
        Result result = BusinessRules.run(checkIfUserExists(user));
        if (result != null) {
            return new ErrorDataResult<>(result.getMessage());
        }
        
        return new SuccessDataResult<>(user, messageService.getMessage(Messages.USER_FOUND));
    }

    // BUSINESS RULES

    private Result checkIfUsernameExists(String username) {
        if (userDao.existsByUsername(username)) {
            return new ErrorResult(messageService.getMessage(Messages.USER_USERNAME_EXISTS));
        }
        return new SuccessResult();
    }

    private Result checkIfEmailExists(String email) {
        if (email != null && userDao.existsByEmail(email)) {
            return new ErrorResult(messageService.getMessage(Messages.USER_EMAIL_EXISTS));
        }
        return new SuccessResult();
    }

    private Result checkIfUserExists(User user) {
        if (user == null) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private UserAdminDto toAdminDto(User user) {
        Long roleId = null;
        String roleCode = null;
        if (user.getRole() != null) {
            roleId = user.getRole().getCodeId();
            roleCode = user.getRole().getCodeValue();
        }
        return new UserAdminDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roleId,
                roleCode,
                user.isPremiumActive(),
                resolvePremiumUntil(user.getId()),
                user.isActive(),
                user.getCreatedAt(),
                user.getDeletedAt()
        );
    }

    private Result checkEmailUpdate(String newEmail, User existingUser) {
        if (existingUser == null) return new SuccessResult();
        
        if (newEmail != null && !newEmail.equals(existingUser.getEmail())) {
            return BusinessRules.run(checkIfEmailExists(newEmail));
        }
        return new SuccessResult();
    }

    private LocalDateTime resolvePremiumUntil(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return userSubscriptionDao.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(s -> s.getStatus() != UserSubscriptionStatus.CANCELED)
                .map(s -> s.getExpiresAt())
                .filter(expiresAt -> expiresAt != null && expiresAt.isAfter(now))
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private UserDto toUserDtoWithComputedPremium(User user) {
        UserDto dto = userMapper.entityToDto(user);
        dto.setPremiumUntil(resolvePremiumUntil(user.getId()));
        return dto;
    }
}
