package com.algangi.mongle.comment.presentation.mapper;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.presentation.dto.CommentInfoResponse;
import com.algangi.mongle.comment.presentation.dto.ReplyInfoResponse;
import com.algangi.mongle.member.domain.Member;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentMapper {

    private static final String MASKED_CONTENT = "삭제된 댓글입니다.";
    private static final String MASKED_NICKNAME = "(알 수 없음)";
    private static final String DEFAULT_PROFILE_IMAGE_URL = "default_profile_image_url";

    public static CommentInfoResponse toCommentInfoResponse(Comment comment, Long currentMemberId, boolean hasReplies) {
        BaseInfo baseInfo = createBaseInfo(comment, currentMemberId);
        return new CommentInfoResponse(
                comment.getId(),
                baseInfo.content(),
                baseInfo.authorNickname(),
                baseInfo.authorProfileImageUrl(),
                baseInfo.likeCount(),
                baseInfo.dislikeCount(),
                comment.getCreatedDate(),
                baseInfo.isAuthor(),
                baseInfo.isDeleted(),
                hasReplies
        );
    }

    public static ReplyInfoResponse toReplyInfoResponse(Comment reply, Long currentMemberId) {
        BaseInfo baseInfo = createBaseInfo(reply, currentMemberId);
        return new ReplyInfoResponse(
                reply.getId(),
                baseInfo.content(),
                baseInfo.authorNickname(),
                baseInfo.authorProfileImageUrl(),
                baseInfo.likeCount(),
                baseInfo.dislikeCount(),
                reply.getCreatedDate(),
                baseInfo.isAuthor(),
                baseInfo.isDeleted()
        );
    }

    private static BaseInfo createBaseInfo(Comment comment, Long currentMemberId) {
        return comment.isDeleted()
                ? BaseInfo.createForDeleted()
                : BaseInfo.createForActive(comment, currentMemberId);
    }

    private record BaseInfo(
            String content,
            String authorNickname,
            String authorProfileImageUrl,
            long likeCount,
            long dislikeCount,
            boolean isAuthor,
            boolean isDeleted
    ) {

        static BaseInfo createForDeleted() {
            return new BaseInfo(MASKED_CONTENT, MASKED_NICKNAME, DEFAULT_PROFILE_IMAGE_URL, 0L, 0L, false, true);
        }

        static BaseInfo createForActive(Comment comment, Long currentMemberId) {
            Member author = comment.getMember();
            boolean isAuthor = author != null && Objects.equals(author.getMemberId(), currentMemberId);
            String nickname = (author != null) ? author.getNickname() : MASKED_NICKNAME;
            String profileImage = (author != null) ? author.getProfileImage() : DEFAULT_PROFILE_IMAGE_URL;

            return new BaseInfo(comment.getContent(), nickname, profileImage, comment.getLikeCount(), comment.getDislikeCount(), isAuthor, false);
        }
    }
}