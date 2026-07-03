package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.SocialPlatformLookup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ISocialPlatformLookupDao extends JpaRepository<SocialPlatformLookup, Long> {
    List<SocialPlatformLookup> findByActiveTrueOrderBySortOrderAsc();

    List<SocialPlatformLookup> findAllByOrderBySortOrderAsc();

    Optional<SocialPlatformLookup> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}
