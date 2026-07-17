package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.DiscoveryTabOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDiscoveryTabOptionDao extends JpaRepository<DiscoveryTabOption, Long> {
    List<DiscoveryTabOption> findByActiveTrueOrderBySortOrderAsc();
    List<DiscoveryTabOption> findAllByOrderBySortOrderAsc();
    Optional<DiscoveryTabOption> findByCode(String code);
    boolean existsByCode(String code);
}
