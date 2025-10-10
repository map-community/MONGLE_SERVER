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
public class ProfileImageHandler implements FileHandler {

    private static final String TEMP_DIR_PREFIX = "temp/profiles/";
    private static final String PROFILE_IMAGE_DIR = "profiles/images/";
    private static final String DELIMITER = "/";

    private static final long MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    private static final int MAX_FILE_COUNT = 1;

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");


    @Override
    public FileType getFileType() {
        return FileType.PROFILE_IMAGE;
    }

    @Override
    public void validateFiles(List<FileMetadata> files) {
        validateMaxFileCount(files);
        files.forEach(this::validateFile);
    }

    private void validateMaxFileCount(List<FileMetadata> files) {
        if (files.size() > MAX_FILE_COUNT) {
            throw new ApplicationException(FileErrorCode.INVALID_FILE_COUNT);
        }
    }

    private void validateFile(FileMetadata file) {
        if (file.isImage() && file.fileSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new ApplicationException(FileErrorCode.INVALID_IMAGE_SIZE);
        }
    }

    @Override
    public String generateTempKey(String fileName) {
        String extension = Files.getFileExtension(fileName).toLowerCase();
        validateExtension(extension);
        return TEMP_DIR_PREFIX + UUID.randomUUID()
            + "."
            + extension;
    }

    private void validateExtension(String extension) {
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new ApplicationException(FileErrorCode.INVALID_FILE_EXTENSION);
        }
    }

    private String extractFileName(String s3Key) {
        return s3Key.substring(s3Key.lastIndexOf(DELIMITER) + 1);
    }

    @Override
    public String generatePermanentKey(String domainId, String tempKey) {
        if (tempKey == null || !tempKey.startsWith(TEMP_DIR_PREFIX)) {
            throw new ApplicationException(FileErrorCode.INVALID_TEMPORARY_KEY);
        }
        String fileName = extractFileName(tempKey);
        String permanentDir = getPermanentDirectory(fileName);
        return permanentDir + domainId + DELIMITER + fileName;
    }

    private String getPermanentDirectory(String fileName) {
        String extension = Files.getFileExtension(fileName).toLowerCase();
        if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return PROFILE_IMAGE_DIR;
        }
        throw new ApplicationException(FileErrorCode.INVALID_FILE_EXTENSION);
    }

    @Override
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
        throw new ApplicationException(FileErrorCode.INVALID_FILE_EXTENSION);
    }
}
