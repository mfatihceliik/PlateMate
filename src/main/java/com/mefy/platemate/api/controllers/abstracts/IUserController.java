package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.UserDto;
import com.mefy.platemate.entities.dto.request.UpdateUserRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/users")
public interface IUserController {

    @GetMapping
    ResponseEntity<DataResult<List<UserDto>>> getAll();

    @GetMapping("/{id}")
    ResponseEntity<DataResult<UserDto>> getById(@PathVariable Long id);

    @GetMapping("/search")
    ResponseEntity<DataResult<UserDto>> getByUsername(@RequestParam String username);

    @PutMapping
    ResponseEntity<Result> update(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateUserRequest request
    );

    @DeleteMapping("/{id}")
    ResponseEntity<Result> delete(@PathVariable Long id);
}
