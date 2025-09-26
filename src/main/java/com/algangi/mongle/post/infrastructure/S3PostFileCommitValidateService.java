package com.algangi.mongle.post.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.post.domain.PostFileUploadConstants;
import com.algangi.mongle.post.domain.service.PostFileCommitValidateService;
import com.algangi.mongle.post.domain.service.PostFileKeyGenerator;
import com.algangi.mongle.post.exception.PostFileErrorCode;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

@Service
@RequiredArgsConstructor
public class S3PostFileCommitValidateService implements PostFileCommitValidateService {

    private final S3Client s3Client;
    private final PostFileKeyGenerator postFileKeyGenerator;
    @Value("${mongle.aws.s3.bucket}")
    private String bucket;

    @Override
    public void validateTemporaryFile(String tempKey) {
        if (!tempKey.startsWith(PostFileUploadConstants.TEMP_DIR)) {
            throw new ApplicationException(PostFileErrorCode.INVALID_TEMPORARY_KEY);
        }
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucket)
                .key(tempKey)
                .build());
        } catch (Exception e) {
            throw new ApplicationException(PostFileErrorCode.FILE_NOT_FOUND_STORAGE)
                .addErrorInfo("key", tempKey);
        }
    }

}