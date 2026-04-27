package com.mefy.platemate.entities.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String username;
    
    private String email;

    @NotBlank(message = "{validation.user.password.notblank}")
    private String password;
    
    public String getIdentifier() {
        if (email != null && !email.trim().isEmpty()) {
            return email;
        }
        return username;
    }
}
