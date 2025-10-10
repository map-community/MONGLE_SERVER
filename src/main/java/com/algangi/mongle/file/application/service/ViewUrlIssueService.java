package com.algangi.mongle.file.application.service;

import java.util.List;

import com.algangi.mongle.file.application.dto.PresignedUrl;

public interface ViewUrlIssueService {

    List<PresignedUrl> issueViewUrls(List<String> fileKeyList);

    PresignedUrl issueViewUrl(String fileKey);
}
