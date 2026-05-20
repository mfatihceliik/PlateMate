package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.dataAccess.projections.PlateSearchAggregateProjection;
import com.mefy.platemate.entities.concrete.PlateSearchEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IPlateSearchEventDao extends JpaRepository<PlateSearchEvent, Long> {

    long countBySearchedAtGreaterThanEqualAndSearchedAtLessThan(LocalDateTime start, LocalDateTime end);

    long deleteBySearchedAtBefore(LocalDateTime cutoff);

    long countByPlateIdAndSearchedAtGreaterThanEqualAndSearchedAtLessThan(
            Long plateId,
            LocalDateTime start,
            LocalDateTime end
    );

    long countByPlateId(Long plateId);

    @Query("""
            select max(e.searchedAt)
            from PlateSearchEvent e
            where e.plate.id = :plateId
              and e.searchedAt >= :start
              and e.searchedAt < :end
            """)
    LocalDateTime findLastSearchedAtByPlateIdAndWindow(
            @Param("plateId") Long plateId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select max(e.searchedAt)
            from PlateSearchEvent e
            where e.plate.id = :plateId
            """)
    LocalDateTime findLastSearchedAtByPlateId(@Param("plateId") Long plateId);

    @Query("""
            select e.plate.id as plateId,
                   count(e.id) as searchCount,
                   max(e.searchedAt) as lastSearchedAt
            from PlateSearchEvent e
            where e.searchedAt >= :start and e.searchedAt < :end
            group by e.plate.id
            """)
    List<PlateSearchAggregateProjection> getDailySearchAggregates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
