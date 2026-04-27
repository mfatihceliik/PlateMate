package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IVehicleDao extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByPlateCode(String plateCode);
    boolean existsByPlateCode(String plateCode);
    List<Vehicle> findByUserId(Long userId);
}
