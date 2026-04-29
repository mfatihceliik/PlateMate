package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IReportService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.core.utilities.mappers.ReportMapper;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.*;
import com.mefy.platemate.dataAccess.abstracts.IReportDao;
import com.mefy.platemate.dataAccess.abstracts.IUserDao;
import com.mefy.platemate.entities.concrete.Report;
import com.mefy.platemate.entities.concrete.ReportStatus;
import com.mefy.platemate.entities.concrete.User;
import com.mefy.platemate.entities.dto.ReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportManager implements IReportService {

    private final IReportDao reportDao;
    private final IUserDao userDao;
    private final ReportMapper reportMapper;
    private final IMessageService messageService;

    @Override
    public Result add(Report report) {
        Long reporterId = report.getReporter().getId();
        Long reportedUserId = report.getReportedUser().getId();

        User reporter = userDao.findById(reporterId).orElse(null);
        User reportedUser = userDao.findById(reportedUserId).orElse(null);

        Result result = BusinessRules.run(
                checkIfUsersExist(reporter, reportedUser),
                checkIfSelfReport(reporterId, reportedUserId)
        );
        if (result != null) return result;

        report.setReporter(reporter);
        report.setReportedUser(reportedUser);
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());

        reportDao.save(report);
        return new SuccessResult(messageService.getMessage(Messages.REPORT_ADDED));
    }

    @Override
    public Result markAsReviewed(Long reportId) {
        Report report = reportDao.findById(reportId).orElse(null);
        Result result = BusinessRules.run(checkIfReportExists(report));
        if (result != null) return result;

        report.setStatus(ReportStatus.REVIEWED);
        reportDao.save(report);
        return new SuccessResult(messageService.getMessage(Messages.REPORT_REVIEWED));
    }

    @Override
    public Result markAsResolved(Long reportId) {
        Report report = reportDao.findById(reportId).orElse(null);
        Result result = BusinessRules.run(checkIfReportExists(report));
        if (result != null) return result;

        report.setStatus(ReportStatus.RESOLVED);
        reportDao.save(report);
        return new SuccessResult(messageService.getMessage(Messages.REPORT_RESOLVED));
    }

    @Override
    public DataResult<List<ReportDto>> getByReportedUserId(Long reportedUserId) {
        List<Report> reports = reportDao.findByReportedUserId(reportedUserId);
        List<ReportDto> dtos = reports.stream().map(reportMapper::entityToDto).collect(Collectors.toList());
        return new SuccessDataResult<>(dtos, messageService.getMessage(Messages.REPORTS_LISTED));
    }

    @Override
    public DataResult<List<ReportDto>> getAllPending() {
        List<Report> reports = reportDao.findByStatus(ReportStatus.PENDING);
        List<ReportDto> dtos = reports.stream().map(reportMapper::entityToDto).collect(Collectors.toList());
        return new SuccessDataResult<>(dtos, messageService.getMessage(Messages.REPORTS_LISTED));
    }

    /// BUSINESS RULES

    private Result checkIfUsersExist(User reporter, User reportedUser) {
        if (reporter == null || reportedUser == null) {
            return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));
        }
        return new SuccessResult();
    }

    private Result checkIfSelfReport(Long reporterId, Long reportedUserId) {
        if (reporterId.equals(reportedUserId)) {
            return new ErrorResult(messageService.getMessage(Messages.REPORT_SELF_NOT_ALLOWED));
        }
        return new SuccessResult();
    }

    private Result checkIfReportExists(Report report) {
        if (report == null) {
            return new ErrorResult(messageService.getMessage(Messages.REPORT_NOT_FOUND));
        }
        return new SuccessResult();
    }
}
