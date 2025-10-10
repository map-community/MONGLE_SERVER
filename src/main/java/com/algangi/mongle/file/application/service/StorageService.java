package com.algangi.mongle.file.application.service;

import java.util.List;

public interface StorageService {

    String issueUploadPresignedUrl(String s3Key, long expirationMinutes);

    void copyFile(String sourceKey, String destinationKey);

    void deleteFile(String s3Key);

    void deleteBulkFiles(List<String> s3Keys);

    void validateFileExists(String s3Key);
}
