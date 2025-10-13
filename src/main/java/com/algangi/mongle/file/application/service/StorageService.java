package com.algangi.mongle.file.application.service;

import java.util.List;

public interface StorageService {

    String issueUploadPresignedUrl(String fileKey, long expirationMinutes);

    void changeTagToPermanent(String fileKey);

    void deleteFile(String fileKey);

    void deleteBulkFiles(List<String> fileKeys);
}
