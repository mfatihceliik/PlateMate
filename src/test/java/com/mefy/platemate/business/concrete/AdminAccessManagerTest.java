package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserRole;
import com.mefy.platemate.entities.concrete.UserRoleCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAccessManagerTest {

    @Mock
    private IUserDao userDao;
    @Mock
    private IMessageService messageService;

    private AdminAccessManager manager;

    @BeforeEach
    void setUp() {
        manager = new AdminAccessManager(userDao, messageService);
    }

    @Test
    void checkAdminReturnsSuccessForAdminUser() {
        User user = new User();
        UserRole role = new UserRole();
        role.setCode(UserRoleCode.ADMIN);
        user.setRole(role);

        when(userDao.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(user));

        Result result = manager.checkAdmin(1L);

        assertTrue(result.isSuccess());
    }

    @Test
    void checkAdminReturnsErrorForMissingUser() {
        when(userDao.findByIdAndActiveTrue(9L)).thenReturn(Optional.empty());
        when(messageService.getMessage(Messages.USER_NOT_FOUND)).thenReturn("not-found");

        Result result = manager.checkAdmin(9L);

        assertFalse(result.isSuccess());
    }
}
