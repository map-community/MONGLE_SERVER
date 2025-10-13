package com.algangi.mongle.file.domain;

import java.util.List;

import com.algangi.mongle.file.application.dto.FileMetadata;
import com.algangi.mongle.file.presentation.dto.UploadUrlRequest.UploadFileInfo;

public interface FileHandler {

    FileType getFileType();

    void validateFiles(List<FileMetadata> files);

    String generateFileKey(String fileName);

    List<FileMetadata> createMetadata(List<UploadFileInfo> fileInfos);
}
