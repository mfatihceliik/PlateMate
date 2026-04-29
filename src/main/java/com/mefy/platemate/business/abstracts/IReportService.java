package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.Report;
import com.mefy.platemate.entities.dto.ReportDto;

import java.util.List;

public interface IReportService {
    Result add(Report report);
    Result markAsReviewed(Long reportId);
    Result markAsResolved(Long reportId);
    DataResult<List<ReportDto>> getByReportedUserId(Long reportedUserId);
    DataResult<List<ReportDto>> getAllPending();
}
