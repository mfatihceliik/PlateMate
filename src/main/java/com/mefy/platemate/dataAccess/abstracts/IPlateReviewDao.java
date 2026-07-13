package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.dataAccess.projections.CityReviewCountProjection;
import com.mefy.platemate.dataAccess.projections.PlateDailyReviewProjection;
import com.mefy.platemate.dataAccess.projections.PlateRatingAggregateProjection;
import com.mefy.platemate.dataAccess.projections.RecentReviewActivityProjection;
import com.mefy.platemate.entities.concrete.PlateReview;
import com.mefy.platemate.entities.concrete.PlateReviewStatus;
import com.mefy.platemate.entities.concrete.PlateStatus;
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

    Page<PlateReview> findByPlatePlateCodeAndStatusId(String plateCode, Long statusId, Pageable pageable);

    default Page<PlateReview> findByPlatePlateCodeAndStatus(String plateCode, PlateReviewStatus status, Pageable pageable) {
        return findByPlatePlateCodeAndStatusId(plateCode, status == null ? null : status.getId(), pageable);
    }

    @Query("""
            select r.rating as rating, count(r) as cnt
            from PlateReview r
            where r.plate.id = :plateId
              and r.statusId = :statusId
            group by r.rating
            order by r.rating desc
            """)
    List<Object[]> getRatingDistribution(@Param("plateId") Long plateId, @Param("statusId") Long statusId);

    @Query("SELECT r FROM PlateReview r JOIN FETCH r.user u LEFT JOIN FETCH u.profile WHERE r.plate.plateCode = :plateCode AND r.statusId = :statusId ORDER BY r.createdAt DESC")
    List<PlateReview> findRecentByPlateCodeWithUserProfile(@Param("plateCode") String plateCode, @Param("statusId") Long statusId, Pageable pageable);

    Page<PlateReview> findByUserId(Long userId, Pageable pageable);

    Page<PlateReview> findByUserIdAndStatusId(Long userId, Long statusId, Pageable pageable);

    @Query("SELECT r FROM PlateReview r JOIN FETCH r.plate p LEFT JOIN FETCH p.city JOIN FETCH r.user WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<PlateReview> findRecentByUserIdWithPlateAndCity(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT r FROM PlateReview r JOIN FETCH r.plate p LEFT JOIN FETCH p.city JOIN FETCH r.user WHERE r.user.id = :userId AND r.statusId = :statusId ORDER BY r.createdAt DESC")
    List<PlateReview> findRecentByUserIdAndStatusIdWithPlateAndCity(@Param("userId") Long userId, @Param("statusId") Long statusId, Pageable pageable);

    default Page<PlateReview> findByUserIdAndStatus(Long userId, PlateReviewStatus status, Pageable pageable) {
        return findByUserIdAndStatusId(userId, status == null ? null : status.getId(), pageable);
    }

    Page<PlateReview> findByStatusIdOrderByCreatedAtDesc(Long statusId, Pageable pageable);

    default Page<PlateReview> findByStatusOrderByCreatedAtDesc(PlateReviewStatus status, Pageable pageable) {
        return findByStatusIdOrderByCreatedAtDesc(status == null ? null : status.getId(), pageable);
    }

    Optional<PlateReview> findByPlateIdAndUserId(Long plateId, Long userId);

    List<PlateReview> findByPlateIdInOrderByCreatedAtDesc(java.util.Collection<Long> plateIds);

    long countByPlateId(Long plateId);

    long countByStatusId(Long statusId);

    long countByPlateIdAndStatusId(Long plateId, Long statusId);

    default long countByPlateIdAndStatus(Long plateId, PlateReviewStatus status) {
        return countByPlateIdAndStatusId(plateId, status == null ? null : status.getId());
    }

    long countByUserId(Long userId);

    long countByUserIdAndStatusId(Long userId, Long statusId);

    default long countByUserIdAndStatus(Long userId, PlateReviewStatus status) {
        return countByUserIdAndStatusId(userId, status == null ? null : status.getId());
    }

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

    @Query("""
            select count(r.id)
            from PlateReview r
            where r.createdAt >= :start
              and r.createdAt < :end
              and r.statusId = :statusId
              and r.plate.statusId = :plateStatusId
            """)
    long countApprovedByCreatedAtWindowAndStatuses(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statusId") Long statusId,
            @Param("plateStatusId") Long plateStatusId
    );

    default long countApprovedByCreatedAtWindow(LocalDateTime start, LocalDateTime end) {
        return countApprovedByCreatedAtWindowAndStatuses(
                start,
                end,
                PlateReviewStatus.APPROVED.getId(),
                PlateStatus.ACTIVE.getId()
        );
    }

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
              and r.statusId = :statusId
            """)
    LocalDateTime findLastReviewAtByPlateIdAndStatus(
            @Param("plateId") Long plateId,
            @Param("statusId") Long statusId
    );

    default LocalDateTime findLastReviewAtByPlateIdAndStatus(Long plateId, PlateReviewStatus status) {
        return findLastReviewAtByPlateIdAndStatus(plateId, status == null ? null : status.getId());
    }

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
              and r.statusId = :statusId
            """)
    Long sumRatingByPlateIdAndStatus(
            @Param("plateId") Long plateId,
            @Param("statusId") Long statusId
    );

    default Long sumRatingByPlateIdAndStatus(Long plateId, PlateReviewStatus status) {
        return sumRatingByPlateIdAndStatus(plateId, status == null ? null : status.getId());
    }

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
              and r.statusId = :statusId
            """)
    Long sumRatingByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("statusId") Long statusId
    );

    default Long sumRatingByUserIdAndStatus(Long userId, PlateReviewStatus status) {
        return sumRatingByUserIdAndStatus(userId, status == null ? null : status.getId());
    }

    @Query("""
            select r.plate.id as plateId,
                   count(r.id) as reviewCount,
                   coalesce(sum(r.rating), 0) as totalRatingSum
            from PlateReview r
            where r.statusId = :statusId
              and r.plate.statusId = :plateStatusId
            group by r.plate.id
            """)
    List<PlateRatingAggregateProjection> getRatingAggregatesAndStatuses(
            @Param("statusId") Long statusId,
            @Param("plateStatusId") Long plateStatusId
    );

    default List<PlateRatingAggregateProjection> getRatingAggregates() {
        return getRatingAggregatesAndStatuses(
                PlateReviewStatus.APPROVED.getId(),
                PlateStatus.ACTIVE.getId()
        );
    }

    @Query("""
            select r.plate.id as plateId,
                   count(r.id) as reviewCount,
                   max(r.createdAt) as lastReviewAt
            from PlateReview r
            where r.createdAt >= :start and r.createdAt < :end
              and r.statusId = :statusId
              and r.plate.statusId = :plateStatusId
            group by r.plate.id
            """)
    List<PlateDailyReviewProjection> getDailyReviewAggregatesAndStatuses(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statusId") Long statusId,
            @Param("plateStatusId") Long plateStatusId
    );

    default List<PlateDailyReviewProjection> getDailyReviewAggregates(LocalDateTime start, LocalDateTime end) {
        return getDailyReviewAggregatesAndStatuses(
                start,
                end,
                PlateReviewStatus.APPROVED.getId(),
                PlateStatus.ACTIVE.getId()
        );
    }

    @Query("""
            select r.plate.id as plateId,
                   count(r.id) as reviewCount,
                   max(r.createdAt) as lastReviewAt
            from PlateReview r
            where r.plate.city.id = :cityId
              and r.createdAt >= :start
              and r.createdAt < :end
              and r.statusId = :statusId
              and r.plate.statusId = :plateStatusId
            group by r.plate.id
            """)
    List<PlateDailyReviewProjection> getCityDailyReviewAggregatesAndStatuses(
            @Param("cityId") Integer cityId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statusId") Long statusId,
            @Param("plateStatusId") Long plateStatusId
    );

    default List<PlateDailyReviewProjection> getCityDailyReviewAggregates(Integer cityId, LocalDateTime start, LocalDateTime end) {
        return getCityDailyReviewAggregatesAndStatuses(
                cityId,
                start,
                end,
                PlateReviewStatus.APPROVED.getId(),
                PlateStatus.ACTIVE.getId()
        );
    }

    @Query("""
            select r.plate.city.id as cityId,
                   r.plate.city.name as cityName,
                   count(r.id) as reviewCount
            from PlateReview r
            where r.plate.city is not null
              and r.createdAt >= :start
              and r.createdAt < :end
              and r.statusId = :statusId
              and r.plate.statusId = :plateStatusId
            group by r.plate.city.id, r.plate.city.name
            order by count(r.id) desc
            """)
    List<CityReviewCountProjection> getTopCityReviewCountsAndStatuses(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statusId") Long statusId,
            @Param("plateStatusId") Long plateStatusId,
            Pageable pageable
    );

    default List<CityReviewCountProjection> getTopCityReviewCounts(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return getTopCityReviewCountsAndStatuses(
                start,
                end,
                PlateReviewStatus.APPROVED.getId(),
                PlateStatus.ACTIVE.getId(),
                pageable
        );
    }

    @Query("""
            select r.id as reviewId,
                   r.user.username as username,
                   r.plate.plateCode as plateCode,
                   r.rating as rating,
                   r.comment as comment,
                   r.createdAt as createdAt,
                   r.updatedAt as updatedAt
            from PlateReview r
            where r.statusId = :statusId
              and r.plate.statusId = :plateStatusId
            order by r.updatedAt desc
            """)
    List<RecentReviewActivityProjection> getRecentReviewActivitiesAndStatuses(
            @Param("statusId") Long statusId,
            @Param("plateStatusId") Long plateStatusId,
            Pageable pageable
    );

    default List<RecentReviewActivityProjection> getRecentReviewActivities(Pageable pageable) {
        return getRecentReviewActivitiesAndStatuses(
                PlateReviewStatus.APPROVED.getId(),
                PlateStatus.ACTIVE.getId(),
                pageable
        );
    }

    @Query("""
            select r.id as reviewId,
                   r.user.username as username,
                   r.plate.plateCode as plateCode,
                   r.rating as rating,
                   r.comment as comment,
                   r.createdAt as createdAt,
                   r.updatedAt as updatedAt
            from PlateReview r
            where r.statusId = :statusId
              and r.plate.statusId = :plateStatusId
              and (
                (r.updatedAt is not null and r.updatedAt >= :start and r.updatedAt < :end)
                or (r.updatedAt is null and r.createdAt >= :start and r.createdAt < :end)
              )
            order by coalesce(r.updatedAt, r.createdAt) desc
            """)
    List<RecentReviewActivityProjection> getRecentReviewActivitiesByWindowAndStatuses(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("statusId") Long statusId,
            @Param("plateStatusId") Long plateStatusId,
            Pageable pageable
    );

    default List<RecentReviewActivityProjection> getRecentReviewActivitiesByWindow(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return getRecentReviewActivitiesByWindowAndStatuses(
                start,
                end,
                PlateReviewStatus.APPROVED.getId(),
                PlateStatus.ACTIVE.getId(),
                pageable
        );
    }
}
