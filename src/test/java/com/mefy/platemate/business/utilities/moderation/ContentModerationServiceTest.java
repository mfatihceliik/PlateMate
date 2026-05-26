package com.mefy.platemate.business.utilities.moderation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentModerationServiceTest {

    private final ContentModerationService moderationService = new ContentModerationService();

    @Test
    void cleanTextIsAllowedWithoutReview() {
        ContentModerationResult result = moderationService.moderate("  Trafikte cok dikkatliydi   ");

        assertTrue(result.isAllowed());
        assertFalse(result.isRequiresReview());
    }

    @Test
    void profanityTriggersReview() {
        ContentModerationResult result = moderationService.moderate("Bu surucu salak");

        assertTrue(result.isAllowed());
        assertTrue(result.isRequiresReview());
    }

    @Test
    void symbolOnlyTextIsRejected() {
        ContentModerationResult result = moderationService.moderate("!!!");

        assertFalse(result.isAllowed());
    }

    @Test
    void tooLongTextIsRejected() {
        String longText = "x".repeat(251);

        ContentModerationResult result = moderationService.moderate(longText);

        assertFalse(result.isAllowed());
    }
}
