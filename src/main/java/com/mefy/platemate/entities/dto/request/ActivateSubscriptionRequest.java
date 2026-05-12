package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivateSubscriptionRequest implements IDto {
    @NotNull(message = "{validation.subscription.days.notnull}")
    @Min(value = 1, message = "{validation.subscription.days.min}")
    @Max(value = 365, message = "{validation.subscription.days.max}")
    private Integer days;
}
