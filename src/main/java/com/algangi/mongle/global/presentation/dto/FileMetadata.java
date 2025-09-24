package com.algangi.mongle.global.presentation.dto;

import com.algangi.mongle.global.domain.FileUploadConstants;
import com.google.common.io.Files;

public record FileMetadata(
    String fileName,
    Long fileSize,
    FileType fileType
) {

    public static FileMetadata from(String fileName, Long fileSize) {
        String extension = Files.getFileExtension(fileName).toLowerCase();
        return new FileMetadata(fileName, fileSize, determineFileType(extension));
    }

    private static FileType determineFileType(String extension) {
        if (FileUploadConstants.ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return FileType.IMAGE;
        }
        if (FileUploadConstants.ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            return FileType.VIDEO;
        }
        throw new IllegalArgumentException("지원하지 않는 파일 확장자입니다: " + extension);
    }

    public boolean isImage() {
        return this.fileType == FileType.IMAGE;
    }

    public boolean isVideo() {
        return this.fileType == FileType.VIDEO;
    }

    public enum FileType {IMAGE, VIDEO}
}
