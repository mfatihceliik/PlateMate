package com.mefy.platemate.entities.dto.request;

import com.mefy.platemate.entities.abstracts.IDto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyncPlateReportsRequest implements IDto {
    @NotNull(message = "{validation.report.type.codes.notnull}")
    private List<String> reportTypeCodes;
}
