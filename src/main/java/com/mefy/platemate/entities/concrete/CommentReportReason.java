package com.mefy.platemate.entities.concrete;

import java.util.Arrays;

public enum CommentReportReason {
    HATE_SPEECH(1L, "HATE_SPEECH"),
    INSULT(2L, "INSULT"),
    FALSE_INFORMATION(3L, "FALSE_INFORMATION"),
    PERSONAL_DATA(4L, "PERSONAL_DATA"),
    THREAT(5L, "THREAT"),
    SPAM(6L, "SPAM"),
    OTHER(7L, "OTHER");

    private final Long id;
    private final String code;

    CommentReportReason(Long id, String code) {
        this.id = id;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public static CommentReportReason fromId(Long id) {
        if (id == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.id.equals(id))
                .findFirst()
                .orElse(null);
    }

    public static CommentReportReason fromCode(String code) {
        if (code == null) return null;
        return Arrays.stream(values())
                .filter(value -> value.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static CommentReportReason resolve(Long id, String code) {
        if (id != null) {
            return fromId(id);
        }
        return fromCode(code);
    }
}
