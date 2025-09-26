package com.algangi.mongle.global.infrastructure;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.algangi.mongle.global.application.service.UploadUrlIssueService;
import com.algangi.mongle.global.domain.service.FileKeyGenerator;
import com.algangi.mongle.global.domain.service.FileUploadValidationService;
import com.algangi.mongle.global.presentation.dto.FileMetadata;
import com.algangi.mongle.global.presentation.dto.IssuedUrlInfo;
import com.algangi.mongle.global.presentation.dto.UploadUrlRequest;
import com.algangi.mongle.global.presentation.dto.UploadUrlResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3UploadUrlIssueService implements UploadUrlIssueService {

    private final S3Presigner s3Presigner;
    private final FileUploadValidationService fileUploadValidationService;
    private final FileKeyGenerator fileKeyGenerator;
    @Value("${mongle.aws.s3.bucket}")
    private String bucket;
    @Value("${mongle.aws.s3.presigned-url-expiration-minutes}")
    private long expirationMinutes;

    @Override
    public UploadUrlResponse issueUploadUrls(UploadUrlRequest dto) {
        List<FileMetadata> files = dto.files().stream()
            .map(fileInfo -> FileMetadata.of(fileInfo.fileName(), fileInfo.fileSize()))
            .toList();

        fileUploadValidationService.validateFileCollection(files);

        List<IssuedUrlInfo> issuedUrls = files.stream().map(this::issueUploadUrl)
            .toList();

        return UploadUrlResponse.of(issuedUrls);
    }

    private IssuedUrlInfo issueUploadUrl(FileMetadata fileMetadata) {
        String s3Key = fileKeyGenerator.generateTemporaryKey(fileMetadata.fileName());

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
    }


}
