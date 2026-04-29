package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IReportsController;

import com.mefy.platemate.business.abstracts.IReportService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.Report;
import com.mefy.platemate.entities.dto.ReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReportsController implements IReportsController {

    private final IReportService reportService;

    @Override
    public ResponseEntity<Result> add(@RequestBody Report report) {
        Result result = reportService.add(report);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @Override
    public ResponseEntity<Result> markAsReviewed(@PathVariable Long reportId) {
        Result result = reportService.markAsReviewed(reportId);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @Override
    public ResponseEntity<Result> markAsResolved(@PathVariable Long reportId) {
        Result result = reportService.markAsResolved(reportId);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @Override
    public ResponseEntity<DataResult<List<ReportDto>>> getByReportedUserId(@PathVariable Long reportedUserId) {
        DataResult<List<ReportDto>> result = reportService.getByReportedUserId(reportedUserId);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }

    @Override
    public ResponseEntity<DataResult<List<ReportDto>>> getAllPending() {
        DataResult<List<ReportDto>> result = reportService.getAllPending();
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }
}
