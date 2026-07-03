package com.mefy.platemate.entities.dto;

import com.mefy.platemate.entities.abstracts.IDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyPlateListsDto implements IDto {
    private List<PlateListItemDto> savedPlates;
    private List<PlateListItemDto> alarmPlates;
}
