package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.MyPlateListsDto;

public interface IUserPlateListService {
    Result savePlate(Long userId, String plateCode);

    Result unsavePlate(Long userId, String plateCode);

    Result createAlarm(Long userId, String plateCode);

    Result removeAlarm(Long userId, String plateCode);

    DataResult<MyPlateListsDto> getMyLists(Long userId);
}
