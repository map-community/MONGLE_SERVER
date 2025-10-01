package com.algangi.mongle.reaction.application.service;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.service.CommentFinder;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.reaction.application.event.ReactionUpdatedEvent;
import com.algangi.mongle.reaction.domain.model.ReactionType;
import com.algangi.mongle.reaction.domain.model.TargetType;
import com.algangi.mongle.reaction.presentation.dto.ReactionResponse;
import com.algangi.mongle.stats.application.service.ContentStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionApplicationService {

    private final ContentStatsService contentStatsService;
    private final PostFinder postFinder;
    private final CommentFinder commentFinder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReactionResponse updateReaction(String targetTypeStr, String targetId, String memberId, ReactionType reactionType) {
        // 1. 타겟 타입 변환
        TargetType targetType = TargetType.from(targetTypeStr);

        // 2. 댓글이면 postId 조회, 게시글이면 null (존재 유무 검증 포함)
        String postIdForRanking = resolvePostIdIfComment(targetType, targetId);

        // 3. Redis 반영
        ReactionResponse response = contentStatsService.updateReaction(
                targetType,
                targetId,
                memberId,
                reactionType,
                postIdForRanking
        );

        // 4. 이벤트 발행
        publishReactionEvent(memberId, targetId, targetType, reactionType);

        return response;
    }

    private String resolvePostIdIfComment(TargetType targetType, String targetId) {
        if (targetType == TargetType.COMMENT) {
            Comment comment = commentFinder.getCommentOrThrow(targetId);
            return comment.getPost().getId();
        } else if (targetType == TargetType.POST) {
            postFinder.getPostOrThrow(targetId);
        } else {
            throw new IllegalStateException("유효하지 않은 targetType 입니다: " + targetType);
        }
        return null;
    }

    private void publishReactionEvent(String memberId, String targetId, TargetType targetType, ReactionType reactionType) {
        eventPublisher.publishEvent(
                new ReactionUpdatedEvent(memberId, targetId, targetType, reactionType)
        );
    }

}