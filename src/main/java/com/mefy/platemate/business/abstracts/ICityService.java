package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.concrete.City;

import java.util.List;

public interface ICityService {
    DataResult<List<City>> getAll();
    DataResult<City> getById(Integer id);
}
