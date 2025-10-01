package com.algangi.mongle.report.application.service;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.service.CommentFinder;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.domain.MemberStatus;
import com.algangi.mongle.member.service.MemberFinder;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.report.domain.model.Report;
import com.algangi.mongle.report.domain.model.ReportStatus;
import com.algangi.mongle.report.domain.repository.ReportRepository;
import com.algangi.mongle.report.exception.ReportErrorCode;
import com.algangi.mongle.report.presentation.dto.ReportCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportCommandService {

    private static final int SANCTION_THRESHOLD = 3;

    private final ReportRepository reportRepository;
    private final MemberFinder memberFinder;
    private final PostFinder postFinder;
    private final CommentFinder commentFinder;

    @Transactional
    public void createReport(String reporterId, ReportCreateRequest request) {
        Member reporter = memberFinder.getMemberOrThrow(reporterId);

        String targetAuthorId = getTargetAuthorIdAndValidate(request);

        if (Objects.equals(reporter.getMemberId(), targetAuthorId)) {
            throw new ApplicationException(ReportErrorCode.SELF_REPORT_NOT_ALLOWED);
        }

        if (reportRepository.existsByReporter_MemberIdAndTargetIdAndTargetType(reporterId,
            request.targetId(), request.targetType())) {
            throw new ApplicationException(ReportErrorCode.DUPLICATE_REPORT);
        }

        Report report = Report.builder()
            .reporter(reporter)
            .targetId(request.targetId())
            .targetType(request.targetType())
            .targetAuthorId(targetAuthorId)
            .reason(request.reason())
            .build();

        reportRepository.save(report);

        applySanctionIfNeeded(targetAuthorId);
    }

    private void applySanctionIfNeeded(String targetAuthorId) {
        long reportCount = reportRepository.countByTargetAuthorIdAndStatus(targetAuthorId,
            ReportStatus.RECEIVED);

        if (reportCount >= SANCTION_THRESHOLD) {
            Member targetAuthor = memberFinder.getMemberOrThrow(targetAuthorId);
            if (targetAuthor.getStatus() == MemberStatus.ACTIVE) {
                targetAuthor.ban();
            }
        }
    }

    private String getTargetAuthorIdAndValidate(ReportCreateRequest request) {
        return switch (request.targetType()) {
            case POST -> {
                Post post = postFinder.getPostOrThrow(request.targetId());
                yield post.getAuthorId();
            }
            case COMMENT -> {
                Comment comment = commentFinder.getCommentOrThrow(request.targetId());
                yield comment.getMember().getMemberId();
            }
        };
    }
}