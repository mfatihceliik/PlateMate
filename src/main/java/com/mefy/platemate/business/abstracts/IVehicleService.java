package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.Vehicle;
import com.mefy.platemate.entities.dto.VehicleDto;

import java.util.List;

public interface IVehicleService {
    DataResult<List<VehicleDto>> getAll();
    DataResult<VehicleDto> getByPlateCode(String plateCode);
    DataResult<List<VehicleDto>> getByUserId(Long userId); // Kullanıcının araçları
    Result add(Vehicle vehicle);
    Result update(Vehicle vehicle, Long currentUserId);
    Result delete(Long id, Long currentUserId); // Sadece araç sahibi silebilir
}
