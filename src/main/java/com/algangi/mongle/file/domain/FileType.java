package com.algangi.mongle.file.domain;

import java.util.stream.Stream;

import com.algangi.mongle.file.exception.FileErrorCode;
import com.algangi.mongle.global.exception.ApplicationException;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum FileType {
    POST_FILE,
    PROFILE_IMAGE;

    @JsonCreator
    public static FileType from(String value) {
        if (value == null) {
            return null;
        }

        return Stream.of(FileType.values())
            .filter(type -> type.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new ApplicationException(FileErrorCode.INVALID_FILE_TYPE));
    }
}
