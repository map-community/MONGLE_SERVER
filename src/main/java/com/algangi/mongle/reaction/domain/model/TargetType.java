package com.algangi.mongle.reaction.domain.model;

import java.util.Arrays;

public enum TargetType {
    POST("post"),
    COMMENT("comment");

    private final String lowerCase;

    TargetType(String lowerCase) {
        this.lowerCase = lowerCase;
    }

    public String getLowerCase() {
        return lowerCase;
    }

    public static TargetType from(String value) {
        String preparedValue = value.toUpperCase().endsWith("S")
                ? value.toUpperCase().substring(0, value.length() - 1)
                : value.toUpperCase();

        return Arrays.stream(values())
                .filter(type -> type.name().equals(preparedValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 targetType 입니다: " + value));
    }
}