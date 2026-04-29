package com.mefy.platemate.api.controllers.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.concrete.Report;
import com.mefy.platemate.entities.dto.ReportDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/reports")
public interface IReportsController {

    @PostMapping("/add")
    ResponseEntity<Result> add(@RequestBody Report report);

    @PutMapping("/{reportId}/review")
    ResponseEntity<Result> markAsReviewed(@PathVariable Long reportId);

    @PutMapping("/{reportId}/resolve")
    ResponseEntity<Result> markAsResolved(@PathVariable Long reportId);

    @GetMapping("/reportedUser/{reportedUserId}")
    ResponseEntity<DataResult<List<ReportDto>>> getByReportedUserId(@PathVariable Long reportedUserId);

    @GetMapping("/pending")
    ResponseEntity<DataResult<List<ReportDto>>> getAllPending();
}
