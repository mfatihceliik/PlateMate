package com.mefy.platemate.api.controllers;

import com.mefy.platemate.business.abstracts.IUserService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserDto;
import com.mefy.platemate.entities.dto.request.UpdateUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping
    public ResponseEntity<DataResult<List<UserDto>>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataResult<UserDto>> getById(@PathVariable Long id) {
        DataResult<UserDto> result = userService.getById(id);
        if (!result.isSuccess()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<DataResult<UserDto>> getByUsername(@RequestParam String username) {
        DataResult<UserDto> result = userService.getByUsername(username);
        if (!result.isSuccess()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping
    public ResponseEntity<Result> update(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateUserRequest request
    ) {

        com.mefy.platemate.entities.concrete.User user = new com.mefy.platemate.entities.concrete.User();
        user.setId(currentUserId);
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        Result result = userService.update(user);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result> delete(@PathVariable Long id) {
        Result result = userService.delete(id);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
