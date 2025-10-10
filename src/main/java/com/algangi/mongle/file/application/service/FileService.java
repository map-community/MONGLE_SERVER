package com.algangi.mongle.file.application.service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.algangi.mongle.file.application.dto.FileMetadata;
import com.algangi.mongle.file.application.dto.PresignedUrl;
import com.algangi.mongle.file.domain.FileHandler;
import com.algangi.mongle.file.domain.FileType;
import com.algangi.mongle.file.exception.FileErrorCode;
import com.algangi.mongle.file.presentation.dto.UploadUrlRequest;
import com.algangi.mongle.file.presentation.dto.UploadUrlResponse;
import com.algangi.mongle.file.presentation.dto.ViewUrlRequest;
import com.algangi.mongle.file.presentation.dto.ViewUrlResponse;
import com.algangi.mongle.global.exception.ApplicationException;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final Map<FileType, FileHandler> handlerMap = new EnumMap<>(FileType.class);
    private final List<FileHandler> handlers;
    private final StorageService storageService;
    private final ViewUrlIssueService viewUrlIssueService;
    @Value("${mongle.aws.s3.presigned-url-expiration-minutes}")
    private long expirationMinutes;

    @PostConstruct
    public void init() {
        handlerMap.putAll(handlers.stream()
            .collect(Collectors.toMap(FileHandler::getFileType, Function.identity())));
    }

    public UploadUrlResponse issueUploadUrls(UploadUrlRequest request) {
        FileHandler handler = getHandler(request.fileType());

        List<FileMetadata> files = handler.createMetadata(request.files());

        handler.validateFiles(files);

        List<PresignedUrl> issuedUrls = files.stream()
            .map(file -> {
                String tempKey = handler.generateTempKey(file.fileName());
                String url = storageService.issueUploadPresignedUrl(tempKey, expirationMinutes);
                LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
                return new PresignedUrl(tempKey, url, expiresAt);
            }).toList();

        return UploadUrlResponse.of(issuedUrls);
    }

    public ViewUrlResponse issueViewUrls(ViewUrlRequest request) {
        return ViewUrlResponse.of(viewUrlIssueService.issueViewUrls(request.fileKeyList()));
    }

    public List<String> commitFiles(FileType fileType, String domainId, List<String> tempKeys) {
        FileHandler handler = getHandler(fileType);

        return tempKeys.stream()
            .map(tempKey -> {
                storageService.validateFileExists(tempKey);
                String permanentKey = handler.generatePermanentKey(domainId, tempKey);
                storageService.copyFile(tempKey, permanentKey);
                storageService.deleteFile(tempKey);
                return permanentKey;
            }).toList();
    }

    public void validateTemporaryFilesExist(List<String> tempKeys) {
        if (tempKeys == null || tempKeys.isEmpty()) {
            return;
        }
        tempKeys.parallelStream().forEach(storageService::validateFileExists);
    }

    public void deletePermanentFiles(List<String> fileKeys) {
        storageService.deleteBulkFiles(fileKeys);
    }

    private FileHandler getHandler(FileType fileType) {
        FileHandler handler = handlerMap.get(fileType);
        if (handler == null) {
            throw new ApplicationException(FileErrorCode.INVALID_FILE_TYPE);
        }
        return handler;
    }
}
