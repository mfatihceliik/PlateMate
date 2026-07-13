package com.mefy.platemate.business.utilities.i18n;

import com.mefy.platemate.core.utilities.messages.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a localized display label for a closed enum value (status/reason/severity/…) from
 * {@code messages_<locale>.properties}, keyed as {@code enum.<category>.<CODE>} and resolved by the
 * request's {@code Accept-Language} (via {@link IMessageService} / {@code LocaleContextHolder}).
 *
 * <p>Falls back to the raw {@code code} when no properties key exists, so responses never fail and
 * adding a key is always safe/optional (the MessageSource is not configured with
 * {@code useCodeAsDefaultMessage}).
 */
@Component
@RequiredArgsConstructor
public class LocalizedEnumService {

    private final IMessageService messageService;

    public String label(String category, String code) {
        if (code == null) {
            return null;
        }
        return label(category, code, code);
    }

    /**
     * Same lookup with an explicit fallback (e.g. an admin-entered catalog label)
     * instead of echoing the raw code.
     */
    public String label(String category, String code, String fallback) {
        if (code == null) {
            return fallback;
        }
        return messageService.getMessageOrDefault("enum." + category + "." + code, fallback);
    }
}
