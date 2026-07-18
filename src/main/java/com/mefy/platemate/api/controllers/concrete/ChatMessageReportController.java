package com.mefy.platemate.api.controllers.concrete;

import com.mefy.platemate.api.controllers.abstracts.IChatMessageReportController;
import com.mefy.platemate.business.abstracts.IChatMessageReportService;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.entities.dto.request.AddChatMessageReportRequest;
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
public class ChatMessageReportController implements IChatMessageReportController {

    private final IChatMessageReportService chatMessageReportService;

    @Override
    public ResponseEntity<Result> reportMessage(
            @PathVariable Long messageId,
            @RequestAttribute("userId") Long currentUserId,
            @Valid @RequestBody AddChatMessageReportRequest request) {
        Result result = chatMessageReportService.reportMessage(currentUserId, messageId, request.getReason());
        if (!result.isSuccess()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
