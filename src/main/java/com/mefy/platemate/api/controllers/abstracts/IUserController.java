package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserAdminDto;
import com.mefy.platemate.entities.dto.request.UpdateUserRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/users")
public interface IUserController {

    @GetMapping
    ResponseEntity<DataResult<List<UserAdminDto>>> getAll(@RequestAttribute("userId") Long currentUserId);

    @GetMapping("/{id}")
    ResponseEntity<DataResult<UserAdminDto>> getById(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId
    );

    @GetMapping("/search")
    ResponseEntity<DataResult<List<com.mefy.platemate.entities.dto.UserDto>>> searchUsers(
            @RequestParam String username
    );

    @PutMapping("/{userId}")
    ResponseEntity<Result> update(
            @PathVariable("userId") Long userId,
            @RequestAttribute("userId") Long tokenUserId,
            @Valid @RequestBody UpdateUserRequest request
    );

    @DeleteMapping("/{id}")
    ResponseEntity<Result> delete(
            @PathVariable Long id,
            @RequestAttribute("userId") Long tokenUserId
    );
}
