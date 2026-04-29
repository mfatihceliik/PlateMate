package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.concrete.City;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/cities")
public interface ICityController {

    @GetMapping
    ResponseEntity<DataResult<List<City>>> getAll();

    @GetMapping("/{id}")
    ResponseEntity<DataResult<City>> getById(@PathVariable Integer id);
}
