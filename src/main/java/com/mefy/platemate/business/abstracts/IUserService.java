package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.UserAdminDto;
import com.mefy.platemate.entities.dto.UserDto;

import java.util.List;

public interface IUserService {
    DataResult<User> add(User user);

    DataResult<List<UserDto>> getAll();

    DataResult<List<UserAdminDto>> getAllForAdmin();

    DataResult<UserDto> getById(Long id);

    DataResult<UserAdminDto> getByIdForAdmin(Long id);

    Result update(User user);

    Result delete(Long id);

    DataResult<UserDto> getByUsername(String username);

    DataResult<UserAdminDto> getByUsernameForAdmin(String username);

    DataResult<User> getByUsernameOrEmailForAuth(String identifier);
}
