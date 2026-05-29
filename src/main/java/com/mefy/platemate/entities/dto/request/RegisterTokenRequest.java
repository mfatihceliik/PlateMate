package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterTokenRequest implements IDto {

    @NotBlank(message = "{validation.fcm.token.notblank}")
    private String token;

    @NotBlank(message = "{validation.fcm.deviceId.notblank}")
    private String deviceId;
}
