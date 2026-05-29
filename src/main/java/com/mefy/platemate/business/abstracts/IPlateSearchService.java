package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.Plate;
import com.mefy.platemate.entities.dto.PlateDetailDto;

public interface IPlateSearchService {
    DataResult<PlateDetailDto> searchByPlateCode(String plateCode, Long currentUserId);
    
    Plate getOrCreatePlate(String normalizedPlateCode);
    
    String normalizePlate(String plateCode);
    
    Result checkIfPlateValid(String normalizedPlate);
    
    Result checkIfPlatePubliclyVisible(Plate plate);
}
