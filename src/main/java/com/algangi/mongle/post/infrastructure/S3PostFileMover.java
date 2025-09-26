package com.algangi.mongle.post.infrastructure;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.algangi.mongle.post.domain.service.PostFileKeyGenerator;
import com.algangi.mongle.post.domain.service.PostFileMover;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Component
@RequiredArgsConstructor
public class S3PostFileMover implements PostFileMover {

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
