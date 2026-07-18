package com.mefy.platemate.business.concrete;

import com.mefy.platemate.business.abstracts.IChatMessageReportService;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.business.utilities.rules.BusinessRules;
import com.mefy.platemate.business.utilities.rules.RelationshipRules;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import com.mefy.platemate.dataAccess.abstracts.IChatMessageDao;
import com.mefy.platemate.dataAccess.abstracts.IChatMessageReportDao;
import com.mefy.platemate.dataAccess.abstracts.IParticipantDao;
import com.mefy.platemate.entities.concrete.ChatMessage;
import com.mefy.platemate.entities.concrete.ChatMessageReport;
import com.mefy.platemate.entities.concrete.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageReportManager implements IChatMessageReportService {

    private final IChatMessageReportDao chatMessageReportDao;
    private final IChatMessageDao chatMessageDao;
    private final IParticipantDao participantDao;
    private final IMessageService messageService;
    private final RelationshipRules relationshipRules;

    @Override
    @Transactional
    public Result reportMessage(Long reporterId, Long messageId, String reason) {
        ChatMessage message = chatMessageDao.findById(messageId).orElse(null);
        if (message == null || message.getChatRoom() == null || message.getSender() == null) {
            return new ErrorResult(messageService.getMessage(Messages.MESSAGE_NOT_FOUND));
        }
        if (!participantDao.existsByUserIdAndChatRoomId(reporterId, message.getChatRoom().getId())) {
            return new ErrorResult(messageService.getMessage(Messages.AUTH_UNAUTHORIZED));
        }

        Result guard = BusinessRules.run(
                () -> relationshipRules.notSelf(reporterId, message.getSender().getId(), Messages.CHAT_MESSAGE_REPORT_SELF_NOT_ALLOWED));
        if (guard != null) {
            return guard;
        }

        User reporter = new User();
        reporter.setId(reporterId);

        ChatMessageReport report = new ChatMessageReport();
        report.setReporter(reporter);
        report.setMessage(message);
        report.setReason(reason);
        chatMessageReportDao.save(report);

        return new SuccessResult(messageService.getMessage(Messages.CHAT_MESSAGE_REPORT_SUCCESS));
    }
}
