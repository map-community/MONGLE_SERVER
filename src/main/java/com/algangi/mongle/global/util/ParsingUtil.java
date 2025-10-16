package com.algangi.mongle.global.util;

import java.time.Instant;
import java.util.function.Function;

public final class ParsingUtil {

    private static final int PREVIEW_LIMIT = 20;

    public static <T> T parse(String value, Function<String, T> parser, String errorMsg) {
        String normalized = normalizeInput(value, errorMsg);

        try {
            return parser.apply(normalized);
        } catch (Exception e) {
            throw buildParseException(errorMsg, normalized, e);
        }
    }

    public static Instant parseDate(String value) {
        return parse(value, Instant::parse, "잘못된 날짜 형식");    }

    public static Long parseLong(String value) {
        return parse(value, Long::parseLong, "잘못된 숫자 형식");
    }

    private static String normalizeInput(String value, String errorMsg) {
        if (value == null) {
            throw new IllegalArgumentException(errorMsg + " (입력값이 null입니다)");
        }
        return value.strip();
    }

    private static IllegalArgumentException buildParseException(String errorMsg, String value, Exception e) {
        return new IllegalArgumentException(
                errorMsg + " (입력 일부: '" + preview(value) + "')", e
        );
    }

    private static String preview(String s) {
        if (s == null) return "null";

        return s.length() <= PREVIEW_LIMIT
                ? s
                : s.substring(0, PREVIEW_LIMIT) + "…";
    }

}