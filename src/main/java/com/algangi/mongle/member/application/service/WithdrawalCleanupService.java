package com.algangi.mongle.member.application.service;

import com.algangi.mongle.comment.domain.repository.CommentRepository;
import com.algangi.mongle.member.application.event.MemberWithdrawnEvent;
import com.algangi.mongle.member.repository.MemberRepository;
import com.algangi.mongle.reaction.domain.repository.ReactionRepository;
import com.algangi.mongle.stats.application.service.ContentStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WithdrawalCleanupService {

    private final ContentManagementService contentManagementService;
    private final ContentStatsService contentStatsService;
    private final MemberRepository memberRepository;
    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final ContentManagementDbService dbService;

    // 사용자 프로필 삭제, 소셜 계정 연동 삭제 등 추가 필요
    @Transactional
    public void cleanupDataFor(MemberWithdrawnEvent event) {
        // 리액션 정리
        contentStatsService.removeReactionsFromRedis(event.memberId(), event.reactions());
        reactionRepository.deleteAllByMemberId(event.memberId());

        // 게시글, 댓글 정리
        contentManagementService.cleanupRedisDataForPosts(event.postIds());
        contentManagementService.cleanupRedisDataForComments(
                event.commentIds(),
                event.postCommentCountDelta(),
                event.commentsByPost()
        );

        dbService.updateWithdrawnUserCommentsInDb(event.commentIds(),
                event.postCommentCountDelta());
        dbService.updateWithdrawnUserPostsInDb(event.postIds());

        // postRepository.unlinkMemberFromPosts(event.memberId());
        commentRepository.unlinkMemberFromComments(event.memberId());

        // 멤버 엔티티 삭제
        memberRepository.deleteById(event.memberId());
    }
}