package com.algangi.mongle.global.application.service;

import com.algangi.mongle.file.application.dto.PresignedUrl;

import ViewUrlRequest;
import ViewUrlResponse;

public interface ViewUrlIssueService {

    ViewUrlResponse issueViewUrls(ViewUrlRequest request);

    PresignedUrl issueViewUrl(String fileKey);
}
