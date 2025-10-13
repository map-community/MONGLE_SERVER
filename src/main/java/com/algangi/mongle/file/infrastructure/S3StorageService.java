package com.algangi.mongle.file.infrastructure;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.algangi.mongle.file.application.service.StorageService;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.global.exception.AwsErrorCode;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.S3Error;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Tagging;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    @Value("${mongle.aws.s3.bucket}")
    private String bucket;

    @Override
    public String issueUploadPresignedUrl(String s3Key, long expirationMinutes) {
        if (s3Key == null) {
            throw new IllegalArgumentException("S3 key는 null일 수 없습니다.");
        }
        try {
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .putObjectRequest(builder -> builder
                    .bucket(bucket)
                    .key(s3Key)
                    .tagging("status=temp")
                    .build())
                .build();

            return s3Presigner.presignPutObject(presignRequest).url().toString();
        } catch (S3Exception e) {
            throw new ApplicationException(AwsErrorCode.S3_PRESIGNED_URL_ISSUE_FAILED, e)
                .addErrorInfo("s3Key", s3Key)
                .addErrorInfo("awsErrorMessage", e.getMessage());
        }
    }

    @Override
    public void changeTagToPermanent(String s3Key) {
        if (s3Key == null) {
            return;
        }
        try {
            PutObjectTaggingRequest taggingRequest = PutObjectTaggingRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .tagging(Tagging.builder()
                    .tagSet(Tag.builder().key("status").value("permanent").build())
                    .build())
                .build();
            s3Client.putObjectTagging(taggingRequest);
        } catch (S3Exception e) {
            throw new ApplicationException(AwsErrorCode.S3_FILE_TAGGING_FAILED, e)
                .addErrorInfo("s3Key", s3Key)
                .addErrorInfo("awsErrorMessage", e.getMessage());
        }
    }

    @Override
    public void deleteFile(String s3Key) {
        if (s3Key == null) {
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build());
        } catch (S3Exception e) {
            throw new ApplicationException(AwsErrorCode.S3_FILE_DELETE_FAILED, e)
                .addErrorInfo("s3Key", s3Key)
                .addErrorInfo("awsErrorMessage", e.getMessage());
        }
    }

    @Override
    public void deleteBulkFiles(List<String> s3Keys) {
        if (s3Keys == null || s3Keys.isEmpty()) {
            return;
        }
        try {
            List<ObjectIdentifier> identifiers = s3Keys.stream()
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
                    .addErrorInfo("s3Keys", String.join(", ", s3Keys))
                    .addErrorInfo("failedKeys", String.join(", ", failedKeys))
                    .addErrorInfo("awsErrorMessage", awsMessage);
            }
        } catch (S3Exception e) {
            throw new ApplicationException(AwsErrorCode.S3_FILE_DELETE_FAILED, e)
                .addErrorInfo("bucket", bucket)
                .addErrorInfo("s3Keys", String.join(", ", s3Keys))
                .addErrorInfo("awsErrorMessage", e.getMessage());
        }
    }

}
