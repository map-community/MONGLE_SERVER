package com.algangi.mongle.global.application.service;

import com.algangi.mongle.global.presentation.dto.ViewUrlRequest;
import com.algangi.mongle.global.presentation.dto.ViewUrlResponse;

public interface ViewUrlIssueService {

    ViewUrlResponse issueViewUrls(ViewUrlRequest request);
}
