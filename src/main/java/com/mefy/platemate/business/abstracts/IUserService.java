package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.UserAdminDto;
import com.mefy.platemate.entities.dto.UserDto;
import com.mefy.platemate.entities.dto.UserSearchResultDto;
import java.util.List;
import com.mefy.platemate.entities.dto.request.RegisterRequest;
import com.mefy.platemate.entities.dto.request.UpdateUserRequest;

public interface IUserService {
    DataResult<User> add(User user);

    DataResult<User> register(RegisterRequest request);

    DataResult<List<UserDto>> getAll();

    DataResult<List<UserAdminDto>> getAllForAdmin();

    DataResult<UserDto> getById(Long id);

    DataResult<UserAdminDto> getByIdForAdmin(Long id);

    Result update(Long id, UpdateUserRequest request);

    Result delete(Long id);

    DataResult<UserDto> getByUsername(String username);

    DataResult<List<UserSearchResultDto>> searchByUsername(String query);

    DataResult<UserAdminDto> getByUsernameForAdmin(String username);

    DataResult<User> getByUsernameOrEmailForAuth(String identifier);

    DataResult<User> getByIdForAuth(Long id);
}
