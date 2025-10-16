package com.algangi.mongle.file.domain;

import java.util.List;

import com.algangi.mongle.file.application.dto.FileMetadata;
import com.algangi.mongle.file.presentation.dto.UploadUrlRequest.UploadFileInfo;
import com.google.common.io.Files;

public interface FileHandler {

    int MAX_FILENAME_LENGTH = 50;

    FileType getFileType();

    void validateFiles(List<FileMetadata> files);

    String generateFileKey(String fileName);

    List<FileMetadata> createMetadata(List<UploadFileInfo> fileInfos);

    default String sanitizeFileName(String originalFileName) {
        String name = Files.getNameWithoutExtension(originalFileName);
        String extension = Files.getFileExtension(originalFileName).toLowerCase();

        String sanitized = name.toLowerCase().replaceAll("\\s+", "-");
        sanitized = sanitized.replaceAll("[^a-z0-9-_]", "");
        sanitized = sanitized.replaceAll("--+", "-");

        if (sanitized.length() > MAX_FILENAME_LENGTH) {
            sanitized = sanitized.substring(0, MAX_FILENAME_LENGTH);
        }
        return sanitized + "." + extension;
    }
}
