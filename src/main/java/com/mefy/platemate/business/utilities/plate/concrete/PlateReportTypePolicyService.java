package com.mefy.platemate.business.utilities.plate.concrete;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

@Service
public class PlateReportTypePolicyService {

    private static final Map<String, String> LEGACY_TO_NEUTRAL_CODE = Map.ofEntries(
            Map.entry("AGGRESSIVE_DRIVER", "TRAFFIC_RULE_VIOLATION"),
            Map.entry("RED_LIGHT_VIOLATION", "TRAFFIC_RULE_VIOLATION"),
            Map.entry("WRONG_WAY", "TRAFFIC_RULE_VIOLATION"),
            Map.entry("DRUNK_DRIVING", "TRAFFIC_RULE_VIOLATION"),
            Map.entry("PHONE_USAGE", "TRAFFIC_RULE_VIOLATION"),
            Map.entry("SPEEDING", "SPEEDING_REPORT"),
            Map.entry("ILLEGAL_PARKING", "PARKING_ISSUE"),
            Map.entry("HIT_AND_RUN", "TRAFFIC_RULE_VIOLATION")
    );

    private static final Map<String, String> NEUTRAL_LABELS = Map.ofEntries(
            Map.entry("UNSAFE_LANE_CHANGE", "Unsafe Lane Change Report"),
            Map.entry("SPEEDING_REPORT", "Speeding Report"),
            Map.entry("PARKING_ISSUE", "Parking Issue"),
            Map.entry("HONKING_DISTURBANCE", "Honking Disturbance"),
            Map.entry("TRAFFIC_RULE_VIOLATION", "Traffic Rule Violation"),
            Map.entry("POSITIVE_DRIVER", "Positive Driver"),
            Map.entry("HELPFUL_DRIVER", "Helpful Driver"),
            Map.entry("RESPECTFUL_DRIVER", "Respectful Driver"),
            Map.entry("THANK_YOU_REPORT", "Thank You Report")
    );

    private static final Map<String, String> NEUTRAL_DESCRIPTIONS = Map.ofEntries(
            Map.entry("UNSAFE_LANE_CHANGE", "Unsafe or abrupt lane-change behavior reported."),
            Map.entry("SPEEDING_REPORT", "Potential speeding behavior reported."),
            Map.entry("PARKING_ISSUE", "Parking behavior affecting traffic flow reported."),
            Map.entry("HONKING_DISTURBANCE", "Excessive honking disturbance reported."),
            Map.entry("TRAFFIC_RULE_VIOLATION", "Traffic rule violation behavior reported."),
            Map.entry("POSITIVE_DRIVER", "Positive and considerate driving behavior reported."),
            Map.entry("HELPFUL_DRIVER", "Helpful behavior in traffic reported."),
            Map.entry("RESPECTFUL_DRIVER", "Respectful and safe behavior reported."),
            Map.entry("THANK_YOU_REPORT", "General appreciation report.")
    );

    public String normalizeIncomingCode(String rawCode) {
        if (rawCode == null) return null;
        String normalized = rawCode.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) return normalized;
        return LEGACY_TO_NEUTRAL_CODE.getOrDefault(normalized, normalized);
    }

    public String neutralLabel(String code, String fallbackLabel) {
        if (code == null) return fallbackLabel;
        String neutralCode = normalizeIncomingCode(code);
        return NEUTRAL_LABELS.getOrDefault(neutralCode, fallbackLabel);
    }

    public String neutralDescription(String code, String fallbackDescription) {
        if (code == null) return fallbackDescription;
        String neutralCode = normalizeIncomingCode(code);
        return NEUTRAL_DESCRIPTIONS.getOrDefault(neutralCode, fallbackDescription);
    }
}
