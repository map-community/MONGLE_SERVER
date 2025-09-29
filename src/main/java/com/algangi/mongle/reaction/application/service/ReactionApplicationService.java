package com.algangi.mongle.reaction.application.service;


import com.algangi.mongle.comment.domain.service.CommentFinder;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.reaction.domain.model.ReactionType;
import com.algangi.mongle.reaction.domain.model.TargetType;
import com.algangi.mongle.reaction.presentation.dto.ReactionResponse;
import com.algangi.mongle.stats.application.service.ContentStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionApplicationService {

    private final ContentStatsService contentStatsService;
    private final PostFinder postFinder;
    private final CommentFinder commentFinder;

    @Transactional
    public ReactionResponse updateReaction(String targetTypeStr, String targetId, String memberId, ReactionType reactionType) {
        TargetType targetType = TargetType.from(targetTypeStr);
        validateTargetExists(targetType, targetId);

        return contentStatsService.updateReaction(targetType, targetId, memberId, reactionType);
    }

    private void validateTargetExists(TargetType targetType, String targetId) {
        switch (targetType) {
            case POST -> postFinder.getPostOrThrow(targetId);
            case COMMENT -> commentFinder.getCommentOrThrow(targetId);
            default -> throw new IllegalStateException("유효하지 않은 targetType 입니다: " + targetType);
        }
    }

}