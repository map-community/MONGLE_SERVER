package com.algangi.mongle.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;

@ConfigurationProperties(prefix = "mongle.aws.cloudfront")
public record CloudFrontProperties(
    String domain,
    String keyPairId,
    String privateKeyFilePath,
    @Name("presigned-url-expiration-minutes")
    Long expirationMinutes
) {

}
