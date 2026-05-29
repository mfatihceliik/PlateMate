package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.CityDto;

import java.util.List;

public interface ICityService {
    DataResult<List<CityDto>> getAll();
    DataResult<CityDto> getById(Integer id);
}
