package com.algangi.mongle.file.domain;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.algangi.mongle.file.application.dto.FileMetadata;
import com.algangi.mongle.file.exception.FileErrorCode;
import com.algangi.mongle.file.presentation.dto.UploadUrlRequest.UploadFileInfo;
import com.algangi.mongle.global.exception.ApplicationException;
import com.google.common.io.Files;

@Component
public class PostFileHandler implements FileHandler {

    private static final String POST_IMAGE_DIR = "posts/images/";
    private static final String POST_VIDEO_DIR = "posts/videos/";
    private static final String DELIMITER = "/";

    private static final long MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    private static final long MAX_TOTAL_IMAGE_SIZE_BYTES = 50 * 1024 * 1024; // 50MB
    private static final long MAX_VIDEO_SIZE_BYTES = 100 * 1024 * 1024; // 100MB
    private static final int MAX_FILE_COUNT = 10;
    private static final int MAX_VIDEO_COUNT = 1;

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of("mp4", "mov", "avi");

    @Override
    public FileType getFileType() {
        return FileType.POST_FILE;
    }

    @Override
    public void validateFiles(List<FileMetadata> files) {
        validateMaxFileCount(files);
        validateMaxVideoCount(files);
        validateTotalImageSize(files);
        files.forEach(this::validateEachFileSize);
    }

    private void validateMaxFileCount(List<FileMetadata> files) {
        if (files.size() > MAX_FILE_COUNT) {
            throw new ApplicationException(FileErrorCode.INVALID_FILE_COUNT);
        }
    }

    private void validateMaxVideoCount(List<FileMetadata> files) {
        long videoCount = files.stream()
            .filter(FileMetadata::isVideo)
            .count();
        if (videoCount > MAX_VIDEO_COUNT) {
            throw new ApplicationException(FileErrorCode.INVALID_VIDEO_COUNT);
        }
    }

    private void validateTotalImageSize(List<FileMetadata> files) {
        long totalImageSize = files.stream()
            .filter(FileMetadata::isImage)
            .mapToLong(FileMetadata::fileSize)
            .sum();
        if (totalImageSize > MAX_TOTAL_IMAGE_SIZE_BYTES) {
            throw new ApplicationException(FileErrorCode.INVALID_TOTAL_IMAGE_SIZE);
        }
    }

    private void validateEachFileSize(FileMetadata file) {
        if (file.isImage() && file.fileSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new ApplicationException(FileErrorCode.INVALID_IMAGE_SIZE);
        }
        if (file.isVideo() && file.fileSize() > MAX_VIDEO_SIZE_BYTES) {
            throw new ApplicationException(FileErrorCode.INVALID_VIDEO_SIZE);
        }
    }

    public List<FileMetadata> createMetadata(List<UploadFileInfo> fileInfos) {
        return fileInfos.stream()
            .map(info -> new FileMetadata(
                info.fileName(),
                info.fileSize(),
                determineMediaType(info.fileName())
            ))
            .toList();
    }

    private FileMetadata.MediaType determineMediaType(String fileName) {
        String extension = Files.getFileExtension(fileName).toLowerCase();
        if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return FileMetadata.MediaType.IMAGE;
        }
        if (ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            return FileMetadata.MediaType.VIDEO;
        }
        throw new ApplicationException(FileErrorCode.INVALID_FILE_EXTENSION);
    }


    @Override
    public String generateFileKey(String fileName) {
        String dir = getDirectory(fileName);
        String uniqueName = UUID.randomUUID().toString();
        return dir + uniqueName + DELIMITER + fileName;
    }

    private String getDirectory(String fileName) {
        String extension = Files.getFileExtension(fileName).toLowerCase();
        if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return POST_IMAGE_DIR;
        }
        if (ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            return POST_VIDEO_DIR;
        }
        throw new ApplicationException(FileErrorCode.INVALID_FILE_EXTENSION);
    }
}