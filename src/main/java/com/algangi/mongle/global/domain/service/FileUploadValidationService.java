package com.algangi.mongle.global.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.algangi.mongle.global.domain.FileUploadConstants;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.global.exception.FileErrorCode;
import com.algangi.mongle.global.presentation.dto.FileMetadata;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileUploadValidationService {

    private static void validateTotalImageSize(List<FileMetadata> files) {
        long totalImageSize = files.stream()
            .filter(file -> file.fileType() == FileMetadata.FileType.IMAGE)
            .mapToLong(FileMetadata::fileSize)
            .sum();

        if (totalImageSize > FileUploadConstants.MAX_TOTAL_IMAGE_SIZE_MB) {
            throw new ApplicationException(FileErrorCode.INVALID_TOTAL_IMAGE_SIZE);
        }
    }

    private static void validateMaxVideoCount(List<FileMetadata> files) {
        long videoCount = files.stream()
            .filter(file -> file.fileType() == FileMetadata.FileType.VIDEO)
            .count();

        if (videoCount > FileUploadConstants.MAX_VIDEO_COUNT) {
            throw new ApplicationException(FileErrorCode.INVALID_VIDEO_COUNT);
        }
    }

    private static void validateMaxFileCount(List<FileMetadata> files) {
        if (files.size() > FileUploadConstants.MAX_FILE_COUNT) {
            throw new ApplicationException(FileErrorCode.INVALID_FILE_COUNT);
        }
    }

    private static void validateFile(FileMetadata file) {
        if (file.isImage()) {
            if (file.fileSize() > FileUploadConstants.MAX_IMAGE_SIZE_MB) {
                throw new ApplicationException(FileErrorCode.INVALID_IMAGE_SIZE);
            }
        } else if (file.isVideo()) {
            if (file.fileSize() > FileUploadConstants.MAX_VIDEO_SIZE_MB) {
                throw new ApplicationException(FileErrorCode.INVALID_VIDEO_SIZE);
            }
        }
    }

    public void validateFileCollection(List<FileMetadata> files) {
        // 총 파일 개수 검증
        validateMaxFileCount(files);
        // 총 비디오 개수 검증
        validateMaxVideoCount(files);
        // 총 이미지 사이즈 검증
        validateTotalImageSize(files);
        files.forEach(FileUploadValidationService::validateFile);
    }
}
