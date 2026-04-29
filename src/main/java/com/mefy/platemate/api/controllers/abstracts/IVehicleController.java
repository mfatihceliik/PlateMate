package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.VehicleDto;
import com.mefy.platemate.entities.dto.request.AddVehicleRequest;
import com.mefy.platemate.entities.dto.request.UpdateVehicleRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/vehicles")
public interface IVehicleController {

    @GetMapping
    ResponseEntity<DataResult<List<VehicleDto>>> getAll();

    @GetMapping("/search")
    ResponseEntity<DataResult<VehicleDto>> getByPlateCode(@RequestParam String plate);

    @GetMapping("/user/{userId}")
    ResponseEntity<DataResult<List<VehicleDto>>> getByUserId(@PathVariable Long userId);

    @PostMapping
    ResponseEntity<Result> add(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddVehicleRequest request
    );

    @PutMapping
    ResponseEntity<Result> update(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateVehicleRequest request
    );

    @DeleteMapping("/{id}")
    ResponseEntity<Result> delete(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId
    );
}
