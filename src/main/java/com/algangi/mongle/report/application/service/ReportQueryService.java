package com.algangi.mongle.report.application.service;

import com.algangi.mongle.report.domain.repository.ReportQueryRepository;
import com.algangi.mongle.report.presentation.dto.ReportAdminResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryService {

    private final ReportQueryRepository reportQueryRepository;

    public Page<ReportAdminResponse> getReportList(Pageable pageable) {
        return reportQueryRepository.findAllBy(pageable)
            .map(ReportAdminResponse::from);
    }
}