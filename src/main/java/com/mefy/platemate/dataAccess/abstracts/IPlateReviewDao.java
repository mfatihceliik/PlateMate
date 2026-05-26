package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.dataAccess.projections.CityReviewCountProjection;
import com.mefy.platemate.dataAccess.projections.PlateDailyReviewProjection;
import com.mefy.platemate.dataAccess.projections.PlateRatingAggregateProjection;
import com.mefy.platemate.dataAccess.projections.RecentReviewActivityProjection;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IPlateReviewDao extends JpaRepository<PlateReview, Long> {
    Page<PlateReview> findByPlatePlateCode(String plateCode, Pageable pageable);

    Page<PlateReview> findByPlatePlateCodeAndStatus(String plateCode, PlateReviewStatus status, Pageable pageable);

    Page<PlateReview> findByUserId(Long userId, Pageable pageable);

    Page<PlateReview> findByUserIdAndStatus(Long userId, PlateReviewStatus status, Pageable pageable);

    Page<PlateReview> findByStatusOrderByCreatedAtDesc(PlateReviewStatus status, Pageable pageable);

    Optional<PlateReview> findByPlateIdAndUserId(Long plateId, Long userId);

    List<PlateReview> findByPlateIdInOrderByCreatedAtDesc(java.util.Collection<Long> plateIds);

    long countByPlateId(Long plateId);

    long countByPlateIdAndStatus(Long plateId, PlateReviewStatus status);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, PlateReviewStatus status);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

    @Query("""
            select count(r.id)
            from PlateReview r
            where r.createdAt >= :start
              and r.createdAt < :end
              and r.status = com.mefy.platemate.entities.concrete.PlateReviewStatus.APPROVED
              and r.plate.status = com.mefy.platemate.entities.concrete.PlateStatus.ACTIVE
            """)
    long countApprovedByCreatedAtWindow(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    long countByPlateIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Long plateId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
            select max(r.createdAt)
            from PlateReview r
            where r.plate.id = :plateId
            """)
    LocalDateTime findLastReviewAtByPlateId(@Param("plateId") Long plateId);

    @Query("""
            select max(r.createdAt)
            from PlateReview r
            where r.plate.id = :plateId
              and r.status = :status
            """)
    LocalDateTime findLastReviewAtByPlateIdAndStatus(
            @Param("plateId") Long plateId,
            @Param("status") PlateReviewStatus status
    );

    @Query("""
            select max(r.createdAt)
            from PlateReview r
            where r.plate.id = :plateId
              and r.createdAt >= :start
              and r.createdAt < :end
            """)
    LocalDateTime findLastReviewAtByPlateIdAndWindow(
            @Param("plateId") Long plateId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select coalesce(sum(r.rating), 0)
            from PlateReview r
            where r.plate.id = :plateId
            """)
    Long sumRatingByPlateId(@Param("plateId") Long plateId);

    @Query("""
            select coalesce(sum(r.rating), 0)
            from PlateReview r
            where r.plate.id = :plateId
              and r.status = :status
            """)
    Long sumRatingByPlateIdAndStatus(
            @Param("plateId") Long plateId,
            @Param("status") PlateReviewStatus status
    );

    @Query("""
            select coalesce(sum(r.rating), 0)
            from PlateReview r
            where r.user.id = :userId
            """)
    Long sumRatingByUserId(@Param("userId") Long userId);

    @Query("""
            select coalesce(sum(r.rating), 0)
            from PlateReview r
            where r.user.id = :userId
              and r.status = :status
            """)
    Long sumRatingByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") PlateReviewStatus status
    );

    @Query("""
            select r.plate.id as plateId,
                   count(r.id) as reviewCount,
                   coalesce(sum(r.rating), 0) as totalRatingSum
            from PlateReview r
            where r.status = com.mefy.platemate.entities.concrete.PlateReviewStatus.APPROVED
              and r.plate.status = com.mefy.platemate.entities.concrete.PlateStatus.ACTIVE
            group by r.plate.id
            """)
    List<PlateRatingAggregateProjection> getRatingAggregates();

    @Query("""
            select r.plate.id as plateId,
                   count(r.id) as reviewCount,
                   max(r.createdAt) as lastReviewAt
            from PlateReview r
            where r.createdAt >= :start and r.createdAt < :end
              and r.status = com.mefy.platemate.entities.concrete.PlateReviewStatus.APPROVED
              and r.plate.status = com.mefy.platemate.entities.concrete.PlateStatus.ACTIVE
            group by r.plate.id
            """)
    List<PlateDailyReviewProjection> getDailyReviewAggregates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select r.plate.id as plateId,
                   count(r.id) as reviewCount,
                   max(r.createdAt) as lastReviewAt
            from PlateReview r
            where r.plate.city.id = :cityId
              and r.createdAt >= :start
              and r.createdAt < :end
              and r.status = com.mefy.platemate.entities.concrete.PlateReviewStatus.APPROVED
              and r.plate.status = com.mefy.platemate.entities.concrete.PlateStatus.ACTIVE
            group by r.plate.id
            """)
    List<PlateDailyReviewProjection> getCityDailyReviewAggregates(
            @Param("cityId") Integer cityId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select r.plate.city.id as cityId,
                   r.plate.city.name as cityName,
                   count(r.id) as reviewCount
            from PlateReview r
            where r.plate.city is not null
              and r.createdAt >= :start
              and r.createdAt < :end
              and r.status = com.mefy.platemate.entities.concrete.PlateReviewStatus.APPROVED
              and r.plate.status = com.mefy.platemate.entities.concrete.PlateStatus.ACTIVE
            group by r.plate.city.id, r.plate.city.name
            order by count(r.id) desc
            """)
    List<CityReviewCountProjection> getTopCityReviewCounts(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query("""
            select r.id as reviewId,
                   r.user.username as username,
                   r.plate.plateCode as plateCode,
                   r.rating as rating,
                   r.comment as comment,
                   r.createdAt as createdAt,
                   r.updatedAt as updatedAt
            from PlateReview r
            where r.status = com.mefy.platemate.entities.concrete.PlateReviewStatus.APPROVED
              and r.plate.status = com.mefy.platemate.entities.concrete.PlateStatus.ACTIVE
            order by r.updatedAt desc
            """)
    List<RecentReviewActivityProjection> getRecentReviewActivities(Pageable pageable);

    @Query("""
            select r.id as reviewId,
                   r.user.username as username,
                   r.plate.plateCode as plateCode,
                   r.rating as rating,
                   r.comment as comment,
                   r.createdAt as createdAt,
                   r.updatedAt as updatedAt
            from PlateReview r
            where r.status = com.mefy.platemate.entities.concrete.PlateReviewStatus.APPROVED
              and r.plate.status = com.mefy.platemate.entities.concrete.PlateStatus.ACTIVE
              and (
                (r.updatedAt is not null and r.updatedAt >= :start and r.updatedAt < :end)
                or (r.updatedAt is null and r.createdAt >= :start and r.createdAt < :end)
              )
            order by coalesce(r.updatedAt, r.createdAt) desc
            """)
    List<RecentReviewActivityProjection> getRecentReviewActivitiesByWindow(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}
