package com.mefy.platemate.dataAccess.abstracts;

import com.mefy.platemate.entities.concrete.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserReportDao extends JpaRepository<UserReport, Long> {
}
