package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.dataAccess.projections.PlateReportAggregateProjection;
import com.mefy.platemate.dataAccess.projections.PlateWeightedScoreProjection;
import com.mefy.platemate.dataAccess.projections.RecentReportActivityProjection;
import com.mefy.platemate.entities.concrete.PlateReport;
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

    List<PlateReport> findByPlateIdInAndActiveTrue(java.util.Collection<Long> plateIds);

    long countByActiveTrueAndLastReportedAtGreaterThanEqualAndLastReportedAtLessThan(LocalDateTime start, LocalDateTime end);

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
            group by r.plate.id
            """)
    List<PlateReportAggregateProjection> getActiveReportAggregates(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
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
            group by r.plate.id
            """)
    List<PlateReportAggregateProjection> getCityActiveReportAggregates(
            @Param("cityId") Integer cityId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select r.plate.id as plateId,
                   coalesce(sum(r.reportType.weight), 0) as weightedScore
            from PlateReport r
            where r.active = true
              and r.lastReportedAt >= :start
              and r.lastReportedAt < :end
              and r.plate.id in :plateIds
            group by r.plate.id
            """)
    List<PlateWeightedScoreProjection> getWeightedScoresByPlateIds(
            @Param("plateIds") Collection<Long> plateIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select r.id as reportId,
                   r.user.username as username,
                   r.plate.plateCode as plateCode,
                   r.reportType.code as reportTypeCode,
                   r.reportType.label as reportTypeLabel,
                   r.lastReportedAt as occurredAt
            from PlateReport r
            where r.active = true
            order by r.lastReportedAt desc
            """)
    List<RecentReportActivityProjection> getRecentReportActivities(Pageable pageable);
}
