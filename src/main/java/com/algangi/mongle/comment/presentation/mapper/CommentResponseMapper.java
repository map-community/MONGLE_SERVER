package com.algangi.mongle.comment.presentation.mapper;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.presentation.dto.AuthorInfoResponse;
import com.algangi.mongle.comment.presentation.dto.CommentInfoResponse;
import com.algangi.mongle.member.domain.Member;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class CommentResponseMapper {

    private static final String MASKED_CONTENT = "삭제된 댓글입니다.";
    private static final String MASKED_NICKNAME = "(알 수 없음)";
    private static final String DEFAULT_PROFILE_IMAGE_URL = "default_profile_image_url";

    public CommentInfoResponse toCommentInfoResponse(
            Comment comment,
            String currentMemberId,
            boolean hasReplies,
            long likeCount,
            long dislikeCount) {

        boolean deleted = comment.isDeleted();
        Member author = comment.getMember();

        return new CommentInfoResponse(
                comment.getId(),
                mapContent(comment, deleted),
                mapAuthor(author, deleted),
                mapCount(likeCount, deleted),
                mapCount(dislikeCount, deleted),
                comment.getCreatedDate(),
                mapIsAuthor(author, currentMemberId, deleted),
                deleted,
                hasReplies
        );
    }

    public CommentInfoResponse toCommentInfoResponse(
            Comment reply,
            String currentMemberId,
            long likeCount,
            long dislikeCount) {
        return toCommentInfoResponse(reply, currentMemberId, false, likeCount, dislikeCount);
    }

    private String mapContent(Comment comment, boolean deleted) {
        return deleted ? MASKED_CONTENT : comment.getContent();
    }

    private AuthorInfoResponse mapAuthor(Member author, boolean deleted) {
        if (deleted || author == null) {
            return new AuthorInfoResponse(null, MASKED_NICKNAME, DEFAULT_PROFILE_IMAGE_URL);
        }

        return new AuthorInfoResponse(
                author.getMemberId(),
                author.getNickname(),
                author.getProfileImage()
        );
    }

    private boolean mapIsAuthor(Member author, String currentMemberId, boolean deleted) {
        return !deleted
                && author != null
                && Objects.equals(author.getMemberId(), currentMemberId);
    }

    private long mapCount(long count, boolean deleted) {
        return deleted ? 0 : count;
    }

}