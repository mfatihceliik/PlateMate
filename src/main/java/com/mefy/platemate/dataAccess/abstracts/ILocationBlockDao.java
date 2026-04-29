package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.LocationBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ILocationBlockDao extends JpaRepository<LocationBlock, Long> {
    boolean existsByUserIdAndBlockedUserId(Long userId, Long blockedUserId);
    void deleteByUserIdAndBlockedUserId(Long userId, Long blockedUserId);
    List<LocationBlock> findByUserId(Long userId);
}
