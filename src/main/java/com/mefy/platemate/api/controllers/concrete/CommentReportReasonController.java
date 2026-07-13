package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.ICommentReportReasonController;
import com.mefy.platemate.business.abstracts.ICommentReportReasonService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.entities.dto.CommentReportReasonDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentReportReasonController implements ICommentReportReasonController {

    private final ICommentReportReasonService commentReportReasonService;

    @Override
    public ResponseEntity<DataResult<List<CommentReportReasonDto>>> getAll() {
        DataResult<List<CommentReportReasonDto>> result = commentReportReasonService.getActiveReasons();
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
