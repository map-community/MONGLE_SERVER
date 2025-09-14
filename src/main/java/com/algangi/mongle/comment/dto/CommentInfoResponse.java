package com.algangi.mongle.comment.dto;

import com.algangi.mongle.comment.domain.Comment;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public record CommentInfoResponse(
        Long commentId,
        String content,
        String authorNickname,
        String authorProfileImageUrl,
        long likeCount,
        long dislikeCount,
        LocalDateTime createdAt,
        boolean isAuthor,
        boolean isDeleted,
        List<CommentInfoResponse> replies
) {

    private static final String MASKED_CONTENT = "삭제된 댓글입니다.";
    private static final String MASKED_NICKNAME = "(알 수 없음)";
    private static final String DEFAULT_PROFILE_IMAGE_URL = "default_profile_image_url";

    public static CommentInfoResponse fromParent(Comment parentComment, Long currentMemberId, List<CommentInfoResponse> replies) {
        List<CommentInfoResponse> safeReplies = (replies != null) ? List.copyOf(replies) : List.of();
        return createResponse(parentComment, currentMemberId, safeReplies);
    }

    public static CommentInfoResponse fromChild(Comment childComment, Long currentMemberId) {
        return createResponse(childComment, currentMemberId, Collections.emptyList());
    }

    private static CommentInfoResponse createResponse(Comment comment, Long currentMemberId, List<CommentInfoResponse> replies) {
        boolean isDeleted = comment.isDeleted();
        boolean isAuthor = !isDeleted &&
                currentMemberId != null &&
                currentMemberId.equals(comment.getMember().getMemberId());

        return new CommentInfoResponse(
                comment.getId(),
                isDeleted ? MASKED_CONTENT : comment.getContent(),
                isDeleted ? MASKED_NICKNAME : comment.getMember().getNickname(),
                isDeleted ? DEFAULT_PROFILE_IMAGE_URL : comment.getMember().getProfileImage(),
                isDeleted ? 0 : comment.getLikeCount(),
                isDeleted ? 0 : comment.getDislikeCount(),
                comment.getCreatedDate(),
                isAuthor,
                isDeleted,
                replies
        );
    }
}