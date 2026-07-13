package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.CommentReportReasonAdminDto;
import com.mefy.platemate.entities.dto.CommentReportReasonDto;
import com.mefy.platemate.entities.dto.request.AddCommentReportReasonRequest;
import com.mefy.platemate.entities.dto.request.UpdateCommentReportReasonRequest;

import java.util.List;

public interface ICommentReportReasonService {
    DataResult<List<CommentReportReasonDto>> getActiveReasons();

    DataResult<List<CommentReportReasonAdminDto>> getAllReasons();

    DataResult<CommentReportReasonAdminDto> addReason(AddCommentReportReasonRequest request);

    DataResult<CommentReportReasonAdminDto> updateReason(Long id, UpdateCommentReportReasonRequest request);

    Result setReasonActive(Long id, boolean active);
}
