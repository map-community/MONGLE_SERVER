package com.algangi.mongle.reaction.application.event;

import com.algangi.mongle.comment.domain.repository.CommentRepository;
import com.algangi.mongle.comment.exception.CommentErrorCode;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.service.MemberFinder;
import com.algangi.mongle.post.domain.repository.PostRepository;
import com.algangi.mongle.post.exception.PostErrorCode;
import com.algangi.mongle.reaction.domain.model.Reaction;
import com.algangi.mongle.reaction.domain.model.ReactionType;
import com.algangi.mongle.reaction.domain.model.TargetType;
import com.algangi.mongle.reaction.domain.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReactionEventListener {

    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberFinder memberFinder;

    @Async("persistenceTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReactionUpdate(ReactionUpdatedEvent event) {

        reactionRepository.findByMember_MemberIdAndTargetIdAndTargetType(
                event.memberId(), event.targetId(), event.targetType()
        ).ifPresentOrElse(
                currentReaction -> handleExistingReaction(currentReaction, event.reactionType()),
                () -> handleNewReaction(
                        event.memberId(),
                        event.targetId(),
                        event.targetType(),
                        event.reactionType()
                )
        );
    }

    private void handleExistingReaction(Reaction currentReaction, ReactionType newReactionType) {
        ReactionType oldReactionType = currentReaction.getType();

        // 1. 같은 리액션(취소)
        if (newReactionType == null || oldReactionType == newReactionType) {
            reactionRepository.delete(currentReaction);
            updateTargetCount(currentReaction.getTargetType(), currentReaction.getTargetId(), oldReactionType, -1L);
        }
        // 2. 변경하는 경우
        else {
            currentReaction.changeType(newReactionType);
            // 기존 타입 카운터 -1
            updateTargetCount(currentReaction.getTargetType(), currentReaction.getTargetId(), oldReactionType, -1L);
            // 새 타입 카운터 +1
            updateTargetCount(currentReaction.getTargetType(), currentReaction.getTargetId(), newReactionType, 1L);
        }
    }

    private void handleNewReaction(String memberId, String targetId, TargetType targetType, ReactionType newReactionType) {
        if (newReactionType == null) {
            return;
        }

        Member author = memberFinder.getMemberOrThrow(memberId);
        Reaction newReaction = Reaction.create(
                targetId,
                targetType,
                newReactionType,
                author
        );

        reactionRepository.save(newReaction);
        updateTargetCount(targetType, targetId, newReactionType, 1);
    }

    private void updateTargetCount(TargetType targetType, String targetId, ReactionType reactionType, long delta) {
        if (delta == 0) return;

        final int MAX_RETRIES = 3;
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            attempt++;
            try {
                boolean success = switch (targetType) {
                    case COMMENT -> updateCommentCount(targetId, reactionType, delta);
                    case POST -> updatePostCount(targetId, reactionType, delta);
                    default -> throw new IllegalArgumentException("지원되지 않는 대상 타입입니다: " + targetType);
                };

                if (success) return;
            } catch (org.springframework.dao.OptimisticLockingFailureException e) {
                if (attempt >= MAX_RETRIES) {
                    throw new IllegalArgumentException(
                            "동시성 충돌로 인해 업데이트 실패: " + targetType + " id=" + targetId, e
                    );
                }
                try { Thread.sleep(50L * attempt); } catch (InterruptedException ignored) {}
            }
        }
    }

    private boolean updateCommentCount(String commentId, ReactionType reactionType, long delta) {
        return commentRepository.findById(commentId).map(comment -> {
            if (reactionType == ReactionType.LIKE) {
                comment.increaseLikeCount(delta);
            } else if (reactionType == ReactionType.DISLIKE) {
                comment.increaseDislikeCount(delta);
            }
            commentRepository.saveAndFlush(comment);
            return true;
        }).orElseThrow(() -> new ApplicationException(CommentErrorCode.COMMENT_NOT_FOUND));
    }

    private boolean updatePostCount(String postId, ReactionType reactionType, long delta) {
        return postRepository.findById(postId).map(post -> {
            if (reactionType == ReactionType.LIKE) {
                post.increaseLikeCount(delta);
            } else if (reactionType == ReactionType.DISLIKE) {
                post.increaseDislikeCount(delta);
            }
            postRepository.saveAndFlush(post);
            return true;
        }).orElseThrow(() -> new ApplicationException(PostErrorCode.POST_NOT_FOUND));
    }

}
