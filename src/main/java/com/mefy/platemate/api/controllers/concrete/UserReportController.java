package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IUserReportController;
import com.mefy.platemate.business.abstracts.IUserReportService;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.request.AddUserReportRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserReportController implements IUserReportController {

    private final IUserReportService userReportService;

    @Override
    public ResponseEntity<Result> reportUser(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddUserReportRequest request) {
        Result result = userReportService.reportUser(currentUserId, userId, request.getReason());
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
