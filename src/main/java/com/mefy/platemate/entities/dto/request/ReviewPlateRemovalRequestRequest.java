package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import com.mefy.platemate.entities.concrete.PlateRemovalRequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPlateRemovalRequestRequest implements IDto {
    @NotNull(message = "{validation.plate.removal.status.notnull}")
    private PlateRemovalRequestStatus status;

    @Size(max = 1000, message = "{validation.plate.removal.admin.note.max}")
    private String adminNote;
}
