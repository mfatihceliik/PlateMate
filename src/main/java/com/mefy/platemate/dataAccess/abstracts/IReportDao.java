package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.Report;
import com.mefy.platemate.entities.concrete.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IReportDao extends JpaRepository<Report, Long> {
    List<Report> findByReportedUserId(Long reportedUserId);
    List<Report> findByReporterId(Long reporterId);
    List<Report> findByStatus(ReportStatus status);
}
