package com.mefy.platemate.business.utilities;

import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import com.mefy.platemate.core.utilities.pagination.PaginationRequest;
import com.mefy.platemate.core.utilities.results.ErrorResult;
import com.mefy.platemate.core.utilities.results.Result;
import com.mefy.platemate.core.utilities.results.SuccessResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Sayfalama ve listeleme sorgularında kullanılan limit değerlerini doğrular.
 * Discovery, subscription, arama gibi tüm domainlerde yeniden kullanılabilir.
 */
@Component
@RequiredArgsConstructor
public class LimitValidator {

    private static final int MAX_LIMIT = PaginationRequest.MAX_SIZE;
    private final IMessageService messageService;

    /**
     * Verilen tüm limit değerlerinin geçerli aralıkta olup olmadığını doğrular.
     * 0 veya negatif değerler ile MAX_SIZE üzeri değerler reddedilir.
     */
    public Result validateLimits(int... limits) {
        for (int limit : limits) {
            if (limit <= 0) {
                return new ErrorResult(messageService.getMessage(Messages.PAGINATION_SIZE_MIN_INVALID));
            }
            if (limit > MAX_LIMIT) {
                return new ErrorResult(messageService.getMessage(Messages.PAGINATION_SIZE_MAX_INVALID, MAX_LIMIT));
            }
        }
        return new SuccessResult();
    }
}
