package com.algangi.mongle.post.infrastructure;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.global.exception.AwsErrorCode;
import com.algangi.mongle.post.domain.service.PostFileHandler;
import com.algangi.mongle.post.domain.service.PostFileKeyGenerator;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.S3Error;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
public class S3PostFileHandler implements PostFileHandler {

    private final S3Client s3Client;
    private final PostFileKeyGenerator postFileKeyGenerator;
    @Value("${mongle.aws.s3.bucket}")
    private String bucket;

    public List<String> moveBulkTempToPermanent(String postId, List<String> tempKeys) {
        return tempKeys.stream()
            .map(tempKey -> moveTempToPermanent(postId, tempKey))
            .toList();
    }

    public String moveTempToPermanent(String postId, String tempKey) {
        String permanentKey = postFileKeyGenerator.generatePermanentKey(postId, tempKey);

        try {
            s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(tempKey)
                .destinationBucket(bucket)
                .destinationKey(permanentKey)
                .build());
        } catch (S3Exception e) {
            throw new ApplicationException(AwsErrorCode.S3_FILE_COPY_FAILED, e)
                .addErrorInfo("tempKey", tempKey)
                .addErrorInfo("permanentKey", permanentKey)
                .addErrorInfo("awsErrorMessage", e.getMessage());
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(tempKey)
                .build());
        } catch (S3Exception e) {
            throw new ApplicationException(AwsErrorCode.S3_FILE_DELETE_FAILED, e)
                .addErrorInfo("tempKey", tempKey)
                .addErrorInfo("permanentKey", permanentKey)
                .addErrorInfo("awsErrorMessage", e.getMessage());
        }

        return permanentKey;
    }

    @Override
    public void deletePermanentFiles(List<String> fileKeys) {
        if (fileKeys == null || fileKeys.isEmpty()) {
            return;
        }

        try {
            List<ObjectIdentifier> identifiers = fileKeys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder().objects(identifiers).build())
                .build();

            DeleteObjectsResponse response = s3Client.deleteObjects(deleteRequest);
            if (response.hasErrors()) {
                List<String> failedKeys = response.errors().stream()
                    .map(S3Error::key)
                    .toList();
                String awsMessage = response.errors().getFirst().message();
                throw new ApplicationException(AwsErrorCode.S3_FILE_DELETE_FAILED)
                    .addErrorInfo("bucket", bucket)
                    .addErrorInfo("fileKeys", String.join(", ", fileKeys))
                    .addErrorInfo("failedKeys", String.join(", ", failedKeys))
                    .addErrorInfo("awsErrorMessage", awsMessage);
            }
        } catch (S3Exception e) {
            throw new ApplicationException(AwsErrorCode.S3_FILE_DELETE_FAILED, e)
                .addErrorInfo("bucket", bucket)
                .addErrorInfo("fileKeys", String.join(", ", fileKeys))
                .addErrorInfo("awsErrorMessage", e.getMessage());
        }
    }

}
