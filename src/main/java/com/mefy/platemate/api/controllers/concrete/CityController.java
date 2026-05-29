package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.ICityController;
import com.mefy.platemate.business.abstracts.ICityService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.CityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CityController implements ICityController {

    private final ICityService cityService;

    @Override
    public ResponseEntity<DataResult<List<CityDto>>> getAll() {
        DataResult<List<CityDto>> result = cityService.getAll();
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @Override
    public ResponseEntity<DataResult<CityDto>> getById(Integer id) {
        DataResult<CityDto> result = cityService.getById(id);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }
}
