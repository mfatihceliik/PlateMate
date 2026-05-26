package com.mefy.platemate.business.utilities.moderation;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ContentModerationService {

    private static final int MAX_COMMENT_LENGTH = 250;

    private static final Pattern MULTI_SPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)(?:\\+?90\\s?)?(?:5\\d{2}|[2-4]\\d{2})\\s?\\d{3}\\s?\\d{2}\\s?\\d{2}(?!\\d)");
    private static final Pattern TC_KIMLIK_PATTERN = Pattern.compile("(?<!\\d)[1-9]\\d{10}(?!\\d)");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile(
            "(?i)(mahalle|mah\\.|sokak|sk\\.|cadde|cd\\.|bulvar|apt\\.|apartman|daire|no\\s*:?\\s*\\d+)"
    );
    private static final Pattern EXCESSIVE_REPEAT_PATTERN = Pattern.compile("(.)\\1{5,}");

    private static final Set<String> PROFANITY_OR_INSULT_KEYWORDS = Set.of(
            "salak", "aptal", "gerizekali", "serefsiz", "mal",
            "orospu", "pic", "siktir", "ibne", "amk", "mk"
    );
    private static final Set<String> THREAT_KEYWORDS = Set.of(
            "oldururum", "vururum", "seni bulurum", "hesabini sorarim"
    );
    private static final Set<String> HEAVY_ACCUSATION_KEYWORDS = Set.of(
            "alkollu", "uyusturucu", "katil", "hirsiz", "dolandirici", "suclu"
    );

    public ContentModerationResult moderate(String rawText) {
        String sanitized = sanitize(rawText);
        List<String> reasons = new ArrayList<>();

        if (sanitized.isBlank()) {
            reasons.add("EMPTY_CONTENT");
            return new ContentModerationResult(false, false, reasons, sanitized);
        }

        if (sanitized.length() > MAX_COMMENT_LENGTH) {
            reasons.add("MAX_LENGTH_EXCEEDED");
            return new ContentModerationResult(false, false, reasons, sanitized);
        }

        if (!containsLetterOrDigit(sanitized)) {
            reasons.add("EMOJI_OR_SYMBOL_ONLY");
            return new ContentModerationResult(false, false, reasons, sanitized);
        }

        String lowered = sanitized.toLowerCase(Locale.ROOT);

        if (containsAnyKeyword(lowered, PROFANITY_OR_INSULT_KEYWORDS)) {
            reasons.add("PROFANITY_OR_INSULT");
        }
        if (containsAnyKeyword(lowered, THREAT_KEYWORDS)) {
            reasons.add("THREAT");
        }
        if (containsAnyKeyword(lowered, HEAVY_ACCUSATION_KEYWORDS)) {
            reasons.add("HEAVY_ACCUSATION");
        }
        if (PHONE_PATTERN.matcher(sanitized).find()) {
            reasons.add("PERSONAL_DATA_PHONE");
        }
        if (TC_KIMLIK_PATTERN.matcher(sanitized).find()) {
            reasons.add("PERSONAL_DATA_TC_ID");
        }
        if (ADDRESS_PATTERN.matcher(sanitized).find()) {
            reasons.add("PERSONAL_DATA_ADDRESS");
        }
        if (EXCESSIVE_REPEAT_PATTERN.matcher(lowered).find()) {
            reasons.add("SPAM_REPEATED_CHARACTERS");
        }

        boolean requiresReview = !reasons.isEmpty();
        return new ContentModerationResult(true, requiresReview, reasons, sanitized);
    }

    private String sanitize(String rawText) {
        if (rawText == null) return "";
        return MULTI_SPACE_PATTERN.matcher(rawText.trim()).replaceAll(" ");
    }

    private boolean containsLetterOrDigit(String text) {
        return text.codePoints().anyMatch(Character::isLetterOrDigit);
    }

    private boolean containsAnyKeyword(String text, Set<String> keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
