package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IUserReportService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.business.utilities.rules.RelationshipRules;
import com.mefy.platemate.business.utilities.rules.UserRules;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.DataResult;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IUserReportDao;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.concrete.UserReport;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserReportManager implements IUserReportService {

    private final IUserReportDao userReportDao;
    private final IMessageService messageService;
    private final UserRules userRules;
    private final RelationshipRules relationshipRules;

    @Override
    @Transactional
    public Result reportUser(Long reporterId, Long reportedId, String reason) {
        Result guard = BusinessRules.run(
                () -> relationshipRules.notSelf(reporterId, reportedId, Messages.USER_REPORT_SELF_NOT_ALLOWED));
        if (guard != null) {
            return guard;
        }

        DataResult<User> reporterResult = userRules.resolveActiveUser(reporterId);
        if (!reporterResult.isSuccess()) {
            return new ErrorResult(reporterResult.getMessage());
        }
        DataResult<User> reportedResult = userRules.resolveActiveUser(reportedId);
        if (!reportedResult.isSuccess()) {
            return new ErrorResult(reportedResult.getMessage());
        }
        User reporter = reporterResult.getData();
        User reported = reportedResult.getData();

        UserReport report = new UserReport();
        report.setReporter(reporter);
        report.setReported(reported);
        report.setReason(reason);
        userReportDao.save(report);

        return new SuccessResult(messageService.getMessage(Messages.USER_REPORT_SUCCESS));
    }
}
