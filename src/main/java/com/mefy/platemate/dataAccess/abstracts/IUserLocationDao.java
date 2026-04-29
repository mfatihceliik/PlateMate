package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserLocationDao extends JpaRepository<UserLocation, Long> {
    Optional<UserLocation> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
