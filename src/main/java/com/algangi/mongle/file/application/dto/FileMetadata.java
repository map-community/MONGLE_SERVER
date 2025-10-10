package com.algangi.mongle.post.application.dto;

import com.algangi.mongle.post.domain.PostFileUploadConstants;
import com.google.common.io.Files;

public record FileMetadata(
    String fileName,
    Long fileSize,
    FileType fileType
) {

    public static FileMetadata of(String fileName, Long fileSize) {
        return new FileMetadata(fileName, fileSize, FileType.from(fileName));
    }

    public boolean isImage() {
        return this.fileType == FileType.IMAGE;
    }

    public boolean isVideo() {
        return this.fileType == FileType.VIDEO;
    }

    public enum FileType {
        IMAGE, VIDEO;

        public static FileType from(String fileName) {
            String extension = Files.getFileExtension(fileName).toLowerCase();
            if (PostFileUploadConstants.ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
                return FileType.IMAGE;
            }
            if (PostFileUploadConstants.ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
                return FileType.VIDEO;
            }
            throw new IllegalArgumentException("지원하지 않는 파일 확장자입니다: " + extension);
        }
    }
}
