package com.mefy.platemate.business.abstracts;

import com.mefy.platemate.core.utilities.results.Result;

public interface IChatMessageReportService {
    Result reportMessage(Long reporterId, Long messageId, String reason);
}
