package com.algangi.mongle.global.config;

import com.algangi.mongle.global.util.TsidConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final TsidConverter tsidConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(tsidConverter);
    }

}