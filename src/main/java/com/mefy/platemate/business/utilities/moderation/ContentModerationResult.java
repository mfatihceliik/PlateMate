package com.mefy.platemate.business.utilities.moderation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContentModerationResult {
    private final boolean allowed;
    private final boolean requiresReview;
    private final List<String> reasons;
    private final String sanitizedText;

    public ContentModerationResult(
            boolean allowed,
            boolean requiresReview,
            List<String> reasons,
            String sanitizedText
    ) {
        this.allowed = allowed;
        this.requiresReview = requiresReview;
        this.reasons = reasons == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(reasons));
        this.sanitizedText = sanitizedText;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public boolean isRequiresReview() {
        return requiresReview;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public String getSanitizedText() {
        return sanitizedText;
    }
}
