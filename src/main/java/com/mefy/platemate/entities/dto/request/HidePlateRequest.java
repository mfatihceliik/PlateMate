package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HidePlateRequest implements IDto {
    @NotBlank(message = "{validation.plate.hide.reason.notblank}")
    @Size(max = 500, message = "{validation.plate.hide.reason.max}")
    private String reason;
}
