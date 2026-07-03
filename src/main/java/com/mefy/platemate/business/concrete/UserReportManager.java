package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IUserReportService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
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
    private final IUserDao userDao;
    private final IMessageService messageService;

    @Override
    @Transactional
    public Result reportUser(Long reporterId, Long reportedId, String reason) {
        if (reporterId.equals(reportedId)) {
            return new ErrorResult(messageService.getMessage(Messages.USER_REPORT_SELF_NOT_ALLOWED));
        }

        User reporter = userDao.findByIdAndActiveTrue(reporterId).orElse(null);
        if (reporter == null) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }

        User reported = userDao.findByIdAndActiveTrue(reportedId).orElse(null);
        if (reported == null) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }

        UserReport report = new UserReport();
        report.setReporter(reporter);
        report.setReported(reported);
        report.setReason(reason);
        userReportDao.save(report);

        return new SuccessResult(messageService.getMessage(Messages.USER_REPORT_SUCCESS));
    }
}
