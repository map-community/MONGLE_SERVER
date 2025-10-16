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
        sanitized = sanitized.replaceAll("^-+|-+$", ""); // 앞뒤 대시 제거

        // 빈 파일명 처리
        if (sanitized.isEmpty()) {
            sanitized = "file";
        }

        // 확장자를 포함한 전체 길이 제한
        int maxNameLength = extension.isEmpty()
            ? MAX_FILENAME_LENGTH
            : MAX_FILENAME_LENGTH - extension.length() - 1;
        if (maxNameLength > 0 && sanitized.length() > maxNameLength) {
            sanitized = sanitized.substring(0, maxNameLength);
        }

        return extension.isEmpty()
            ? sanitized
            : sanitized + "." + extension;
    }
}
