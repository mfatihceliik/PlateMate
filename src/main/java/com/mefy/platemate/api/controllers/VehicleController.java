package com.mefy.platemate.api.controllers;

import com.mefy.platemate.business.abstracts.ICityService;
import com.mefy.platemate.business.abstracts.IVehicleService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.City;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.Vehicle;
import com.mefy.platemate.entities.dto.VehicleDto;
import com.mefy.platemate.entities.dto.request.AddVehicleRequest;
import com.mefy.platemate.entities.dto.request.UpdateVehicleRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final IVehicleService vehicleService;
    private final ICityService cityService;

    @GetMapping
    public ResponseEntity<DataResult<List<VehicleDto>>> getAll() {
        return ResponseEntity.ok(vehicleService.getAll());
    }

    @GetMapping("/search")
    public ResponseEntity<DataResult<VehicleDto>> getByPlateCode(@RequestParam String plate) {
        DataResult<VehicleDto> result = vehicleService.getByPlateCode(plate);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<DataResult<List<VehicleDto>>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(vehicleService.getByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Result> add(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddVehicleRequest request
    ) {

        // City'yi bul
        DataResult<City> cityResult = cityService.getById(request.getCityId());
        if (!cityResult.isSuccess()) {
            return ResponseEntity.badRequest().body(cityResult);
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setPlateCode(request.getPlateCode());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setColor(request.getColor());
        vehicle.setCity(cityResult.getData());

        // User entity'yi sadece ID ile set et
        User owner = new User();
        owner.setId(currentUserId);
        vehicle.setUser(owner);

        Result result = vehicleService.add(vehicle);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping
    public ResponseEntity<Result> update(
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody UpdateVehicleRequest request
    ) {

        DataResult<City> cityResult = cityService.getById(request.getCityId());
        if (!cityResult.isSuccess()) {
            return ResponseEntity.badRequest().body(cityResult);
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setId(request.getId());
        vehicle.setPlateCode(request.getPlateCode());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setColor(request.getColor());
        vehicle.setCity(cityResult.getData());

        Result result = vehicleService.update(vehicle, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result> delete(
            @PathVariable Long id,
            @RequestAttribute("userId") Long currentUserId
    ) {
        Result result = vehicleService.delete(id, currentUserId);
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
