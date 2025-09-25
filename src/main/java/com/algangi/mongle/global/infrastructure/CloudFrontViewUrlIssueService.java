package com.algangi.mongle.global.infrastructure;

import java.util.List;

import org.springframework.stereotype.Service;

import com.algangi.mongle.global.application.service.ViewUrlIssueService;
import com.algangi.mongle.global.config.CloudFrontProperties;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.global.exception.AwsErrorCode;
import com.algangi.mongle.global.presentation.dto.IssuedUrlInfo;
import com.algangi.mongle.global.presentation.dto.ViewUrlRequest;
import com.algangi.mongle.global.presentation.dto.ViewUrlResponse;

import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CustomSignerRequest;

@Service
public class CloudFrontViewUrlIssueService implements ViewUrlIssueService {

    public static final String HTTPS = "https://";
    public static final String DIR_DELIMITER = "/";
    private final CloudFrontProperties cloudFrontProperties;
    private final CloudFrontUtilities cloudFrontUtilities;

    public CloudFrontViewUrlIssueService(CloudFrontProperties cloudFrontProperties) {
        this.cloudFrontProperties = cloudFrontProperties;
        this.cloudFrontUtilities = CloudFrontUtilities.create();
    }

    private static PrivateKey loadPrivateKey(String filePath) {
        try {
            byte[] keyBytes;
            try (FileInputStream fis = new FileInputStream(filePath)) {
                keyBytes = fis.readAllBytes();
            }
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalArgumentException("private Key를 로드할 수 없습니다,");
        }
    }

    @Override
    public ViewUrlResponse issueViewUrls(ViewUrlRequest request) {
        List<IssuedUrlInfo> issuedUrls = request.s3KeySet().stream()
            .map(this::issueViewUrl)
            .toList();

        return ViewUrlResponse.of(issuedUrls);
    }

    public IssuedUrlInfo issueViewUrl(String s3Key) {
        try {
            String resourceUrl = HTTPS + cloudFrontProperties.domain() + DIR_DELIMITER + s3Key;
            Instant expirationTime = Instant.now()
                .plus(cloudFrontProperties.expirationMinutes(), ChronoUnit.MINUTES);

            CustomSignerRequest signerRequest = CustomSignerRequest.builder()
                .resourceUrl(resourceUrl)
                .privateKey(loadPrivateKey(cloudFrontProperties.privateKeyFilePath()))
                .keyPairId(cloudFrontProperties.keyPairId())
                .expirationDate(expirationTime)
                .build();

            String issuedUrl = cloudFrontUtilities.getSignedUrlWithCustomPolicy(signerRequest)
                .toString();
            LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(cloudFrontProperties.expirationMinutes());
            return new IssuedUrlInfo(s3Key, issuedUrl, expiresAt);

        } catch (Exception e) {
            throw new ApplicationException(AwsErrorCode.CLOUDFRONT_ERROR)
                .addErrorInfo("errorDetail", e.getMessage());
        }
    }
}
