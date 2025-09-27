package com.algangi.mongle.post.infrastructure;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.post.domain.PostFileUploadConstants;
import com.algangi.mongle.post.domain.service.PostFileCommitValidationService;
import com.algangi.mongle.post.exception.PostFileErrorCode;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
public class S3PostFileCommitValidationService implements PostFileCommitValidationService {

    private final S3Client s3Client;
    @Value("${mongle.aws.s3.bucket}")
    private String bucket;

    @Override
    public void validateTemporaryFiles(List<String> temporaryFileKeys) {
        if (temporaryFileKeys == null || temporaryFileKeys.isEmpty()) {
            return;
        }
        temporaryFileKeys.forEach(this::validateTemporaryFile);
    }

    private void validateTemporaryFile(String tempKey) {
        if (!tempKey.startsWith(PostFileUploadConstants.TEMP_DIR)) {
            throw new ApplicationException(PostFileErrorCode.INVALID_TEMPORARY_KEY);
        }
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucket)
                .key(tempKey)
                .build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new ApplicationException(PostFileErrorCode.FILE_NOT_FOUND_STORAGE)
                    .addErrorInfo("key", tempKey);
            }
            throw e;
        }
    }

}