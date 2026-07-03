package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.PlateAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IPlateAlarmDao extends JpaRepository<PlateAlarm, Long> {

    boolean existsByUserIdAndPlateId(Long userId, Long plateId);

    Optional<PlateAlarm> findByUserIdAndPlateId(Long userId, Long plateId);

    long countByUserId(Long userId);

    @Query("SELECT pa FROM PlateAlarm pa JOIN FETCH pa.plate p LEFT JOIN FETCH p.city WHERE pa.user.id = :userId ORDER BY pa.createdAt DESC")
    List<PlateAlarm> findByUserIdWithPlate(@Param("userId") Long userId);
}
