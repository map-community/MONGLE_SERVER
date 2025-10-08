package com.algangi.mongle.global.infrastructure;

import java.util.List;

import org.springframework.stereotype.Service;

import com.algangi.mongle.global.application.service.ViewUrlIssueService;
import com.algangi.mongle.global.config.CloudFrontProperties;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.global.exception.AwsErrorCode;
import com.algangi.mongle.global.util.PemUtils;
import com.algangi.mongle.post.application.dto.IssuedUrlInfo;
import com.algangi.mongle.post.presentation.dto.ViewUrlRequest;
import com.algangi.mongle.post.presentation.dto.ViewUrlResponse;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import software.amazon.awssdk.services.cloudfront.CloudFrontUtilities;
import software.amazon.awssdk.services.cloudfront.model.CannedSignerRequest;
import software.amazon.awssdk.services.cloudfront.model.CloudFrontException;

@Service
public class S3ViewUrlIssueService implements ViewUrlIssueService {

    public static final String HTTPS = "https://";
    public static final String DIR_DELIMITER = "/";
    private final CloudFrontProperties cloudFrontProperties;
    private final CloudFrontUtilities cloudFrontUtilities;
    private PrivateKey privateKey;

    public S3ViewUrlIssueService(CloudFrontProperties cloudFrontProperties) {
        this.cloudFrontProperties = cloudFrontProperties;
        this.cloudFrontUtilities = CloudFrontUtilities.create();
    }

    @PostConstruct
    public void init() {
        try {
            this.privateKey = PemUtils.loadPrivateKey(cloudFrontProperties.privateKeyFilePath());
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Private Key 로딩 중 오류 발생", e);
        }
    }

    @Override
    public ViewUrlResponse issueViewUrls(ViewUrlRequest request) {
        List<IssuedUrlInfo> issuedUrls = request.fileKeyList().stream()
            .map(this::issueViewUrl)
            .toList();

        return ViewUrlResponse.of(issuedUrls);
    }

    @Override
    public IssuedUrlInfo issueViewUrl(String fileKey) {
        String resourceUrl = HTTPS + cloudFrontProperties.domain() + DIR_DELIMITER + fileKey;
        Instant expirationTime = Instant.now()
            .plus(cloudFrontProperties.expirationMinutes(), ChronoUnit.MINUTES);

        try {
            CannedSignerRequest signerRequest = CannedSignerRequest.builder()
                .resourceUrl(resourceUrl)
                .privateKey(privateKey)
                .keyPairId(cloudFrontProperties.keyPairId())
                .expirationDate(expirationTime)
                .build();

            String issuedUrl = cloudFrontUtilities.getSignedUrlWithCannedPolicy(signerRequest)
                .url();
            LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(cloudFrontProperties.expirationMinutes());
            return new IssuedUrlInfo(fileKey, issuedUrl, expiresAt);

        } catch (CloudFrontException e) {
            throw new ApplicationException(AwsErrorCode.CLOUDFRONT_PRESIGNED_URL_ISSUE_FAILED, e)
                .addErrorInfo("awsErrorMessage", e.getMessage());
        }
    }

}