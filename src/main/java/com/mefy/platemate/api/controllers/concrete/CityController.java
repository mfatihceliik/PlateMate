package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.ICityController;

import com.mefy.platemate.business.abstracts.ICityService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.concrete.City;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CityController implements ICityController {

    private final ICityService cityService;

    @Override
    public ResponseEntity<DataResult<List<City>>> getAll() {
        return ResponseEntity.ok(cityService.getAll());
    }

    @Override
    public ResponseEntity<DataResult<City>> getById(@PathVariable Integer id) {
        DataResult<City> result = cityService.getById(id);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
