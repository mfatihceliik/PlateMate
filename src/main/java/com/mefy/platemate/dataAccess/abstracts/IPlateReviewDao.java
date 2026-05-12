package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.PlateReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IPlateReviewDao extends JpaRepository<PlateReview, Long> {
    Page<PlateReview> findByPlatePlateCode(String plateCode, Pageable pageable);

    Page<PlateReview> findByUserId(Long userId, Pageable pageable);

    Optional<PlateReview> findByPlateIdAndUserId(Long plateId, Long userId);

    long countByPlateId(Long plateId);

    long countByUserId(Long userId);

    @Query("""
            select coalesce(sum(r.rating), 0)
            from PlateReview r
            where r.plate.id = :plateId
            """)
    Long sumRatingByPlateId(@Param("plateId") Long plateId);

    @Query("""
            select coalesce(sum(r.rating), 0)
            from PlateReview r
            where r.user.id = :userId
            """)
    Long sumRatingByUserId(@Param("userId") Long userId);

    @Query("""
            select r.plate.id as plateId,
                   count(r.id) as reviewCount,
                   coalesce(sum(r.rating), 0) as totalRatingSum
            from PlateReview r
            group by r.plate.id
            """)
    List<PlateRatingAggregateProjection> getRatingAggregates();

    interface PlateRatingAggregateProjection {
        Long getPlateId();

        Long getReviewCount();

        Long getTotalRatingSum();
    }
}
