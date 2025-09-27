package com.algangi.mongle.post.domain.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.algangi.mongle.post.domain.PostFileUploadConstants;
import com.google.common.io.Files;

@Service
public class PostFileKeyGenerator {

    private static final String FILE_NAME_DELIMITER = ".";
    private static final String DIR_DELIMITER = "/";

    public String generateTemporaryKey(String fileName) {
        String extension = Files.getFileExtension(fileName);

        return PostFileUploadConstants.TEMP_DIR
            + UUID.randomUUID()
            + FILE_NAME_DELIMITER
            + extension;
    }

    public String generatePermanentKey(String postId, String tempKey) {
        String fileName = extractFileName(tempKey);
        String permanentDir = getPermanentDirectory(fileName);
        return permanentDir + postId + DIR_DELIMITER + fileName;
    }

    private String getPermanentDirectory(String fileName) {
        String extension = Files.getFileExtension(fileName).toLowerCase();
        if (PostFileUploadConstants.ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return PostFileUploadConstants.POST_IMAGE_DIR;
        }
        if (PostFileUploadConstants.ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
            return PostFileUploadConstants.POST_VIDEO_DIR;
        }
        throw new IllegalArgumentException("지원하지 않는 파일 확장자입니다: " + extension);
    }

    private String extractFileName(String s3Key) {
        return s3Key.substring(s3Key.lastIndexOf(DIR_DELIMITER) + 1);
    }

}
