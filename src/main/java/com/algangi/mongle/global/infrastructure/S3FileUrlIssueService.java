package com.algangi.mongle.global.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.algangi.mongle.global.application.service.FileUrlIssueService;
import com.algangi.mongle.global.domain.FileUploadConstants;
import com.algangi.mongle.global.domain.service.FileUploadValidationService;
import com.algangi.mongle.global.presentation.dto.FileMetadata;
import com.algangi.mongle.global.presentation.dto.FileMetadata.FileType;
import com.algangi.mongle.global.presentation.dto.IssuedUrlInfo;
import com.algangi.mongle.global.presentation.dto.UploadUrlRequest;
import com.algangi.mongle.global.presentation.dto.UploadUrlResponse;
import com.google.common.io.Files;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3FileUrlIssueService implements FileUrlIssueService {

    private static final String FILE_NAME_DELIMITER = ".";
    private final S3Presigner s3Presigner;
    private final FileUploadValidationService fileUploadValidationService;
    @Value("${mongle.aws.s3.bucket}")
    private String bucket;
    @Value("${mongle.aws.s3.presigned-url-expiration-minutes}")
    private long expirationMinutes;

    private String createFileKey(FileMetadata fileMetadata) {
        String dir =
            fileMetadata.fileType() == FileType.IMAGE ?
                FileUploadConstants.POST_IMAGE_DIR :
                FileUploadConstants.POST_VIDEO_DIR;

        String extension = Files.getFileExtension(fileMetadata.fileName());

        return dir + UUID.randomUUID() + FILE_NAME_DELIMITER + extension;
    }

    @Override
    public UploadUrlResponse issueUploadUrls(UploadUrlRequest dto) {
        List<FileMetadata> files = dto.files().stream()
            .map(fileInfo -> FileMetadata.from(fileInfo.fileName(), fileInfo.fileSize()))
            .toList();
        
        fileUploadValidationService.validateFileCollection(files);

        List<IssuedUrlInfo> issuedUrlInfos = files.stream().map(this::createPreSignedUrl)
            .toList();

        return UploadUrlResponse.of(issuedUrlInfos);
    }

    private IssuedUrlInfo createPreSignedUrl(FileMetadata fileMetadata) {
        String s3Key = createFileKey(fileMetadata);

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(expirationMinutes))
            .putObjectRequest(builder -> builder
                .bucket(bucket)
                .key(s3Key)
                .contentLength(fileMetadata.fileSize())
                .build())
            .build();

        String url = s3Presigner.presignPutObject(presignRequest).url().toString();

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        return new IssuedUrlInfo(s3Key, url, expiresAt);
    }


}
