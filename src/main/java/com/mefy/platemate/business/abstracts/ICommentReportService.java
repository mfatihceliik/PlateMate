package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.pagination.PagedData;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.CommentReportDto;
import com.mefy.platemate.entities.dto.request.AddCommentReportRequest;
import com.mefy.platemate.entities.dto.request.ReviewCommentReportRequest;

public interface ICommentReportService {
    Result addReport(Long commentId, Long reporterUserId, AddCommentReportRequest request);

    DataResult<PagedData<CommentReportDto>> getReports(PaginationRequest paginationRequest);

    Result reviewReport(Long reportId, Long reviewerUserId, ReviewCommentReportRequest request);
}
