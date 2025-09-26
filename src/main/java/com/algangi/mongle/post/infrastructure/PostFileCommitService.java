package com.algangi.mongle.post.infrastructure;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.post.domain.PostFileUploadConstants;
import com.algangi.mongle.post.domain.model.PostFile;
import com.algangi.mongle.post.domain.service.PostFileKeyGenerator;
import com.algangi.mongle.post.exception.PostFileErrorCode;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

@Service
@RequiredArgsConstructor
public class PostFileCommitService implements
    com.algangi.mongle.post.application.service.PostFileCommitService {

    private final S3Client s3Client;
    private final PostFileKeyGenerator postFileKeyGenerator;
    @Value("${mongle.aws.s3.bucket}")
    private String bucket;

    @Override
    public List<PostFile> commit(String postId, Set<String> temporaryFileKeys) {
        return temporaryFileKeys.stream()
            .map(tempKey -> {
                validateTemporaryFile(tempKey);
                String permanentKey = moveToPermanentStorage(postId, tempKey);
                return PostFile.create(permanentKey);
            })
            .toList();
    }

    private void validateTemporaryFile(String tempKey) {
        if (!tempKey.startsWith(PostFileUploadConstants.TEMP_DIR)) {
            throw new IllegalArgumentException("유효하지 않은 임시 파일 경로입니다: " + tempKey);
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

    private String moveToPermanentStorage(String postId, String tempKey) {
        String permanentKey = postFileKeyGenerator.generatePermanentKey(postId, tempKey);

        s3Client.copyObject(CopyObjectRequest.builder()
            .sourceBucket(bucket)
            .sourceKey(tempKey)
            .destinationBucket(bucket)
            .destinationKey(permanentKey)
            .build());

        s3Client.deleteObject(DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(tempKey)
            .build());

        return permanentKey;
    }

}