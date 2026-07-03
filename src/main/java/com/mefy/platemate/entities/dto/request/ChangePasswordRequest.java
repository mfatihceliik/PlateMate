package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest implements IDto {

    @NotBlank(message = "{validation.password.current.notblank}")
    private String currentPassword;

    @NotBlank(message = "{validation.password.new.notblank}")
    @Size(min = 8, message = "{validation.password.new.size}")
    @Pattern(regexp = ".*[A-Z].*", message = "{validation.password.new.uppercase}")
    @Pattern(regexp = ".*\\d.*", message = "{validation.password.new.digit}")
    private String newPassword;
}
