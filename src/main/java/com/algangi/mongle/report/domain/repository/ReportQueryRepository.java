package com.algangi.mongle.report.domain.repository;

import com.algangi.mongle.report.domain.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportQueryRepository {

    Page<Report> findAllBy(Pageable pageable);
}