package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.City;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICityDao extends JpaRepository<City, Integer> {
}
