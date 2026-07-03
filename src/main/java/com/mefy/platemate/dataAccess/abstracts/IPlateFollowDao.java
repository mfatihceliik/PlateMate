package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.PlateFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IPlateFollowDao extends JpaRepository<PlateFollow, Long> {

    boolean existsByUserIdAndPlateId(Long userId, Long plateId);

    Optional<PlateFollow> findByUserIdAndPlateId(Long userId, Long plateId);

    long countByUserId(Long userId);

    @Query("SELECT pf.user.id FROM PlateFollow pf WHERE pf.plate.id = :plateId")
    List<Long> findFollowerUserIdsByPlateId(@Param("plateId") Long plateId);
}
