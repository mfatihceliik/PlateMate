package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import com.mefy.platemate.entities.concrete.PlateRemovalRequestReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPlateRemovalRequestRequest implements IDto {
    @NotBlank(message = "{validation.plate.removal.reason.notnull}")
    private String reasonCode;

    @NotBlank(message = "{validation.plate.removal.description.notblank}")
    @Size(max = 1000, message = "{validation.plate.removal.description.max}")
    private String description;

    @Size(max = 255, message = "{validation.plate.removal.requester.email.max}")
    private String requesterEmail;
}
