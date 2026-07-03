package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.SavedPlate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ISavedPlateDao extends JpaRepository<SavedPlate, Long> {

    boolean existsByUserIdAndPlateId(Long userId, Long plateId);

    Optional<SavedPlate> findByUserIdAndPlateId(Long userId, Long plateId);

    long countByUserId(Long userId);

    @Query("SELECT sp FROM SavedPlate sp JOIN FETCH sp.plate p LEFT JOIN FETCH p.city WHERE sp.user.id = :userId ORDER BY sp.createdAt DESC")
    List<SavedPlate> findByUserIdWithPlate(@Param("userId") Long userId);
}
