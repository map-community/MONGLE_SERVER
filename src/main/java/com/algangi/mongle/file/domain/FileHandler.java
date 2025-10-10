package com.algangi.mongle.file.domain;

import java.util.List;

import com.algangi.mongle.file.application.dto.FileMetadata;
import com.algangi.mongle.file.presentation.dto.UploadUrlRequest;

public interface FileHandler {

    FileType getFileType();

    void validateFiles(List<FileMetadata> files);

    String generateTempKey(String fileName);

    String generatePermanentKey(String domainId, String tempKey);

    List<FileMetadata> createMetadata(List<UploadUrlRequest.UploadFileInfo> fileInfos);
}
