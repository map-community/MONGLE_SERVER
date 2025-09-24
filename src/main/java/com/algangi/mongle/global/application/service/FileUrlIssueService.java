package com.algangi.mongle.global.application.service;

import com.algangi.mongle.global.presentation.dto.UploadUrlRequest;
import com.algangi.mongle.global.presentation.dto.UploadUrlResponse;

public interface FileUrlIssueService {

    UploadUrlResponse issueUploadUrls(UploadUrlRequest request);
}
