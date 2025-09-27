package com.algangi.mongle.post.infrastructure;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.algangi.mongle.global.application.service.UploadUrlIssueService;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.global.exception.AwsErrorCode;
import com.algangi.mongle.post.application.dto.FileMetadata;
import com.algangi.mongle.post.application.dto.IssuedUrlInfo;
import com.algangi.mongle.post.domain.service.PostFileKeyGenerator;
import com.algangi.mongle.post.domain.service.PostFileUploadValidationService;
import com.algangi.mongle.post.presentation.dto.UploadUrlRequest;
import com.algangi.mongle.post.presentation.dto.UploadUrlResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3PostFileUploadUrlIssueService implements UploadUrlIssueService {

    private final S3Presigner s3Presigner;
    private final PostFileUploadValidationService postFileUploadValidationService;
    private final PostFileKeyGenerator postFileKeyGenerator;
    @Value("${mongle.aws.s3.bucket}")
    private String bucket;
    @Value("${mongle.aws.s3.presigned-url-expiration-minutes}")
    private long expirationMinutes;

    @Override
    public UploadUrlResponse issueUploadUrls(UploadUrlRequest dto) {
        List<FileMetadata> files = dto.files().stream()
            .map(fileInfo ->
                FileMetadata.of(fileInfo.fileName(), fileInfo.fileSize()))
            .toList();

        postFileUploadValidationService.validateFileCollection(files);

        List<IssuedUrlInfo> issuedUrls = files.stream()
            .map(this::issueUploadUrl)
            .toList();

        return UploadUrlResponse.of(issuedUrls);
    }

    private IssuedUrlInfo issueUploadUrl(FileMetadata fileMetadata) {
        String s3Key = postFileKeyGenerator.generateTemporaryKey(fileMetadata.fileName());

        try {
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .putObjectRequest(builder -> builder
                    .bucket(bucket)
                    .key(s3Key)
                    .build())
                .build();

            String issuedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
            return new IssuedUrlInfo(s3Key, issuedUrl, expiresAt);
        } catch (S3Exception e) {
            throw new ApplicationException(AwsErrorCode.S3_PRESIGNED_URL_ISSUE_FAILED, e)
                .addErrorInfo("s3Key", s3Key)
                .addErrorInfo("awsErrorMessage", e.getMessage());
        }
    }


}
