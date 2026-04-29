package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest implements IDto {
    @Email(message = "{validation.user.email.invalid}")
    private String email;

    @Size(min = 6, message = "{validation.user.password.size}")
    private String password;
}
