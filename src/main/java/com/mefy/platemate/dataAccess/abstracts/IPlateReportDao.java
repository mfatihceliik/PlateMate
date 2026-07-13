package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.dataAccess.projections.PlateReportAggregateProjection;
import com.mefy.platemate.dataAccess.projections.PlateWeightedScoreProjection;
import com.mefy.platemate.dataAccess.projections.RecentReportActivityProjection;
import com.mefy.platemate.dataAccess.projections.ReportTypeCountProjection;
import com.mefy.platemate.entities.concrete.PlateReport;
import com.mefy.platemate.entities.concrete.PlateStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IPlateReportDao extends JpaRepository<PlateReport, Long> {

    Optional<PlateReport> findByPlateIdAndUserIdAndReportTypeId(Long plateId, Long userId, Long reportTypeId);

    List<PlateReport> findByPlateIdAndUserIdAndActiveTrue(Long plateId, Long userId);

    @Query("""
            SELECT r FROM PlateReport r JOIN FETCH r.reportType
            WHERE r.user.id = :userId AND r.plate.id IN :plateIds AND r.active = true
              AND EXISTS (SELECT 1 FROM PlateReview rv
                          WHERE rv.plate.id = r.plate.id
                            AND rv.user.id = r.user.id
                            AND rv.statusId = :approvedStatusId)
            """)
    List<PlateReport> findByUserIdAndPlateIdInAndActiveTrue(
            @Param("userId") Long userId,
            @Param("plateIds") Collection<Long> plateIds,
            @Param("approvedStatusId") Long approvedStatusId);

    List<PlateReport> findByPlateIdInAndActiveTrue(java.util.Collection<Long> plateIds);

    @Query("""
            SELECT r FROM PlateReport r JOIN FETCH r.reportType
            WHERE r.plate.id = :plateId AND r.active = true
              AND EXISTS (SELECT 1 FROM PlateReview rv
                          WHERE rv.plate.id = r.plate.id
                            AND rv.user.id = r.user.id
                            AND rv.statusId = :approvedStatusId)
            """)
    List<PlateReport> findByPlateIdWithReportType(
            @Param("plateId") Long plateId,
            @Param("approvedStatusId") Long approvedStatusId);

    long countByActiveTrueAndLastReportedAtGreaterThanEqualAndLastReportedAtLessThan(LocalDateTime start, LocalDateTime end);

    @Query("""
            select count(r.id)
            from PlateReport r
            where r.active = true
              and r.lastReportedAt >= :start
              and r.lastReportedAt < :end
              and r.plate.statusId = :plateStatusId
            """)
    long countActiveByReportedAtWindowAndPlateStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("plateStatusId") Long plateStatusId
    );



    long countByPlateIdAndActiveTrueAndLastReportedAtGreaterThanEqualAndLastReportedAtLessThan(
            Long plateId,
            LocalDateTime start,
            LocalDateTime end
    );

    long countByPlateIdAndActiveTrue(Long plateId);

    @Query("""
            select coalesce(sum(r.reportType.weight), 0)
            from PlateReport r
            where r.plate.id = :plateId
              and r.active = true
              and r.lastReportedAt >= :start
              and r.lastReportedAt < :end
            """)
    Long getWeightedScoreByPlateIdAndWindow(
            @Param("plateId") Long plateId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select coalesce(sum(r.reportType.weight), 0)
            from PlateReport r
            where r.plate.id = :plateId
              and r.active = true
            """)
    Long getWeightedScoreByPlateId(@Param("plateId") Long plateId);

    @Query("""
            select max(r.lastReportedAt)
            from PlateReport r
            where r.plate.id = :plateId
              and r.active = true
              and r.lastReportedAt >= :start
              and r.lastReportedAt < :end
            """)
    LocalDateTime findLastReportedAtByPlateIdAndWindow(
            @Param("plateId") Long plateId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select max(r.lastReportedAt)
            from PlateReport r
            where r.plate.id = :plateId
              and r.active = true
            """)
    LocalDateTime findLastReportedAtByPlateId(@Param("plateId") Long plateId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update PlateReport r
            set r.active = false,
                r.deactivatedAt = :deactivatedAt,
                r.updatedAt = :updatedAt
            where r.reportType.id = :reportTypeId
              and r.active = true
            """)
    int deactivateActiveReportsByTypeId(
            @Param("reportTypeId") Long reportTypeId,
            @Param("deactivatedAt") LocalDateTime deactivatedAt,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Query("""
            select r.plate.id as plateId,
                   count(r.id) as reportCount,
                   coalesce(sum(r.reportType.weight), 0) as weightedScore,
                   max(r.lastReportedAt) as lastReportedAt
            from PlateReport r
            where r.active = true
              and r.lastReportedAt >= :start
              and r.lastReportedAt < :end
              and r.plate.statusId = :plateStatusId
            group by r.plate.id
            """)
    List<PlateReportAggregateProjection> getActiveReportAggregatesAndPlateStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("plateStatusId") Long plateStatusId
    );



    @Query("""
            select r.plate.id as plateId,
                   count(r.id) as reportCount,
                   coalesce(sum(r.reportType.weight), 0) as weightedScore,
                   max(r.lastReportedAt) as lastReportedAt
            from PlateReport r
            where r.active = true
              and r.lastReportedAt >= :start
              and r.lastReportedAt < :end
              and r.plate.city.id = :cityId
              and r.plate.statusId = :plateStatusId
            group by r.plate.id
            """)
    List<PlateReportAggregateProjection> getCityActiveReportAggregatesAndPlateStatus(
            @Param("cityId") Integer cityId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("plateStatusId") Long plateStatusId
    );



    @Query("""
            select r.plate.id as plateId,
                   coalesce(sum(r.reportType.weight), 0) as weightedScore
            from PlateReport r
            where r.active = true
              and r.lastReportedAt >= :start
              and r.lastReportedAt < :end
              and r.plate.id in :plateIds
              and r.plate.statusId = :plateStatusId
            group by r.plate.id
            """)
    List<PlateWeightedScoreProjection> getWeightedScoresByPlateIdsAndPlateStatus(
            @Param("plateIds") Collection<Long> plateIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("plateStatusId") Long plateStatusId
    );



    @Query("""
            select r.reportType.id as reportTypeId,
                   count(r.id) as reportCount
            from PlateReport r
            where r.active = true
              and r.lastReportedAt >= :start
              and r.lastReportedAt < :end
              and r.plate.statusId = :plateStatusId
            group by r.reportType.id
            order by count(r.id) desc
            """)
    List<ReportTypeCountProjection> getReportTypeCountsByWindowAndPlateStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("plateStatusId") Long plateStatusId,
            Pageable pageable
    );



    @Query("""
            select r.id as reportId,
                   r.user.username as username,
                   r.plate.plateCode as plateCode,
                   r.reportType.id as reportTypeId,
                   r.reportType.code as reportTypeCode,
                   r.reportType.label as reportTypeLabel,
                   r.lastReportedAt as occurredAt
            from PlateReport r
            where r.active = true
              and r.plate.statusId = :plateStatusId
            order by r.lastReportedAt desc
            """)
    List<RecentReportActivityProjection> getRecentReportActivitiesAndPlateStatus(
            @Param("plateStatusId") Long plateStatusId,
            Pageable pageable
    );



    @Query("""
            select r.id as reportId,
                   r.user.username as username,
                   r.plate.plateCode as plateCode,
                   r.reportType.id as reportTypeId,
                   r.reportType.code as reportTypeCode,
                   r.reportType.label as reportTypeLabel,
                   r.lastReportedAt as occurredAt
            from PlateReport r
            where r.active = true
              and r.plate.statusId = :plateStatusId
              and r.lastReportedAt >= :start
              and r.lastReportedAt < :end
            order by r.lastReportedAt desc
            """)
    List<RecentReportActivityProjection> getRecentReportActivitiesByWindowAndPlateStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("plateStatusId") Long plateStatusId,
            Pageable pageable
    );


}
