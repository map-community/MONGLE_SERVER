package com.algangi.mongle.global.util;

import io.micrometer.common.lang.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TsidConverter implements Converter<String, Long> {

    @Override
    public Long convert(@Nullable String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }

        try {
            return Long.parseLong(source);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("유효하지 않은 ID 형식입니다: " + source, e);
        }
    }
}