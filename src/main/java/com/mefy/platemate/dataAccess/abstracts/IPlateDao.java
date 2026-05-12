package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.Plate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IPlateDao extends JpaRepository<Plate, Long> {
    Optional<Plate> findByPlateCode(String plateCode);
}
