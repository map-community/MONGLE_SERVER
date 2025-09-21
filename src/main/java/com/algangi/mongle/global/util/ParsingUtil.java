package com.algangi.mongle.global.util;

import java.time.LocalDateTime;
import java.util.function.Function;

public class ParsingUtil {

    public static <T> T parse(String value, Function<String, T> parser, String errorMsg) {
        try {
            return parser.apply(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(errorMsg + ": " + value, e);
        }
    }

    public static LocalDateTime parseDate(String value) {
        return parse(value, LocalDateTime::parse, "잘못된 날짜 형식");
    }

    public static Long parseLong(String value) {
        return parse(value, Long::parseLong, "잘못된 숫자 형식");
    }
}