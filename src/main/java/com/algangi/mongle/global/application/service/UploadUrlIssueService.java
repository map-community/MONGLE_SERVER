package com.algangi.mongle.global.application.service;

import com.algangi.mongle.post.presentation.dto.UploadUrlRequest;
import com.algangi.mongle.post.presentation.dto.UploadUrlResponse;

public interface UploadUrlIssueService {

    UploadUrlResponse issueUploadUrls(UploadUrlRequest request);
}
