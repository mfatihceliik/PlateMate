package com.mefy.platemate.entities.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "{validation.user.username.notblank}")
    @Size(min = 3, max = 30, message = "{validation.user.username.size}")
    private String username;

    @NotBlank(message = "{validation.user.password.notblank}")
    @Size(min = 6, message = "{validation.user.password.size}")
    private String password;

    @Email(message = "{validation.user.email.invalid}")
    private String email;
}
