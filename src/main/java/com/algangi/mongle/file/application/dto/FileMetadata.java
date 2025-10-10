package com.algangi.mongle.file.application.dto;

public record FileMetadata(
    String fileName,
    Long fileSize,
    MediaType mediaType
) {

    public boolean isImage() {
        return this.mediaType == MediaType.IMAGE;
    }

    public boolean isVideo() {
        return this.mediaType == MediaType.VIDEO;
    }

    public enum MediaType {
        IMAGE, VIDEO;
    }
}
