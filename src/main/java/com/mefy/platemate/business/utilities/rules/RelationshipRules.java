package com.mefy.platemate.business.utilities.rules;

import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Shared guards for directed user↔user / user↔resource relationships (follow, block, report,
 * friendship, plate-follow). The specific message key is passed in so each call-site keeps its
 * exact error message.
 */
@Component
@RequiredArgsConstructor
public class RelationshipRules {

    private final IMessageService messageService;

    /** Fails when {@code a} and {@code b} identify the same subject (self-action not allowed). */
    public Result notSelf(Long a, Long b, String selfNotAllowedKey) {
        if (a != null && a.equals(b)) {
            return new ErrorResult(messageService.getMessage(selfNotAllowedKey));
        }
        return new SuccessResult();
    }

    /** Fails when the relationship already exists. */
    public Result notAlreadyExists(boolean exists, String alreadyExistsKey) {
        if (exists) {
            return new ErrorResult(messageService.getMessage(alreadyExistsKey));
        }
        return new SuccessResult();
    }
}
