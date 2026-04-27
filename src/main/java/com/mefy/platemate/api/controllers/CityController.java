package com.mefy.platemate.api.controllers;

import com.mefy.platemate.business.abstracts.ICityService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.concrete.City;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final ICityService cityService;

    @GetMapping
    public ResponseEntity<DataResult<List<City>>> getAll() {
        return ResponseEntity.ok(cityService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataResult<City>> getById(@PathVariable Integer id) {
        DataResult<City> result = cityService.getById(id);
        if (!result.isSuccess()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
