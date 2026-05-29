package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.CityDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/cities")
@Tag(name = "City Controller", description = "Endpoints for managing cities")
public interface ICityController {

    @Operation(summary = "Get all cities")
    @GetMapping
    ResponseEntity<DataResult<List<CityDto>>> getAll();

    @Operation(summary = "Get city by id")
    @GetMapping("/{id}")
    ResponseEntity<DataResult<CityDto>> getById(@PathVariable("id") Integer id);
}
