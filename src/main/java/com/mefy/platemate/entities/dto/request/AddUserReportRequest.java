package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddUserReportRequest implements IDto {

    @NotBlank(message = "{validation.user.report.reason.notblank}")
    @Size(max = 50, message = "{validation.user.report.reason.max}")
    private String reason;
}
