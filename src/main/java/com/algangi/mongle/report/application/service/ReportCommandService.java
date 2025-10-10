package com.algangi.mongle.report.application.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.service.CommentFinder;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.application.service.ContentManagementService;
import com.algangi.mongle.member.application.service.MemberFinder;
import com.algangi.mongle.member.domain.model.Member;
import com.algangi.mongle.member.domain.model.MemberStatus;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.report.domain.model.Report;
import com.algangi.mongle.report.domain.model.ReportStatus;
import com.algangi.mongle.report.domain.repository.ReportRepository;
import com.algangi.mongle.report.exception.ReportErrorCode;
import com.algangi.mongle.report.presentation.dto.ReportCreateRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportCommandService {

    private static final int SANCTION_THRESHOLD = 3;

    private final ReportRepository reportRepository;
    private final MemberFinder memberFinder;
    private final PostFinder postFinder;
    private final CommentFinder commentFinder;
    private final ContentManagementService contentManagementService;

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

    @Transactional
    public void updateReportStatus(String reportId, ReportStatus newStatus) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ApplicationException(ReportErrorCode.REPORT_NOT_FOUND));

        report.updateStatus(newStatus);
    }

    @Transactional
    public void banUser(String memberId) {
        // Banned 유저의 게시글 및 댓글 처리
        contentManagementService.processPostsOfBannedUser(memberId);
        contentManagementService.processCommentsOfBannedUser(memberId);
    }

    private void applySanctionIfNeeded(String targetAuthorId) {
        long reportCount = reportRepository.countByTargetAuthorIdAndReportStatus(targetAuthorId,
            ReportStatus.RECEIVED);

        if (reportCount >= SANCTION_THRESHOLD) {
            Member targetAuthor = memberFinder.getMemberWithLockOrThrow(targetAuthorId);
            if (targetAuthor.getStatus() == MemberStatus.ACTIVE) {
                targetAuthor.ban();
                banUser(targetAuthorId);
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