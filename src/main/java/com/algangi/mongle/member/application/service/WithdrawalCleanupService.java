package com.algangi.mongle.member.application.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algangi.mongle.auth.application.service.authentication.LogoutService;
import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.repository.CommentRepository;
import com.algangi.mongle.file.application.service.FileService;
import com.algangi.mongle.member.application.event.MemberWithdrawnEvent;
import com.algangi.mongle.member.domain.model.Member;
import com.algangi.mongle.member.domain.repository.MemberRepository;
import com.algangi.mongle.post.domain.repository.PostRepository;
import com.algangi.mongle.reaction.domain.repository.ReactionRepository;
import com.algangi.mongle.reaction.infrastructure.persistence.ReactionRepositoryCustom;
import com.algangi.mongle.stats.application.dto.ReactionCleanupDto;
import com.algangi.mongle.stats.application.service.ContentStatsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WithdrawalCleanupService {

    private final ContentManagementService contentManagementService;
    private final ContentStatsService contentStatsService;
    private final MemberRepository memberRepository;
    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepositoryCustom reactionRepositoryCustom;
    private final ContentManagementDbService dbService;
    private final LogoutService logoutService;
    private final FileService fileService;
    private final MemberFinder memberFinder;

    // 사용자 프로필 삭제, 소셜 계정 연동 삭제, 세션/토큰 무효화 처리 필요
    @Transactional
    public void cleanupDataFor(MemberWithdrawnEvent event) {
        String memberId = event.memberId();

        // 사용자 프로필 삭제
        Member member = memberFinder.getMemberOrThrow(memberId);
        fileService.deletePermanentFiles(List.of(member.getProfileImage()));

        // 소셜 계정 연동 삭제
        //orphanRemoval=true에 의해 Member 엔티티 삭제 시 같이 삭제됨

        // 리프레쉬 토큰 무효화 처리(로그아웃)
        logoutService.logout(memberId);

        // 필요한 테이터 조회
        List<String> postIds = postRepository.findAllIdsByMemberId(memberId);
        List<ReactionCleanupDto> reactions = reactionRepositoryCustom.findAllReactionCleanupData(
            memberId);
        List<Comment> comments = commentRepository.findAllByMember_MemberId(memberId);
        List<String> commentIds = comments.stream().map(Comment::getId).toList();

        // 댓글 처리에 필요한 Map 가공
        Map<String, Long> postCommentCountDelta = comments.stream()
            .collect(Collectors.groupingBy(c -> c.getPost().getId(), Collectors.counting()));
        Map<String, List<String>> commentsByPost = comments.stream()
            .collect(Collectors.groupingBy(c -> c.getPost().getId(),
                Collectors.mapping(Comment::getId, Collectors.toList())));

        // 리액션 정리
        contentStatsService.removeReactionsFromRedis(memberId, reactions);
        reactionRepository.deleteAllByMemberId(memberId);

        // 게시글, 댓글 정리
        contentManagementService.cleanupRedisDataForPosts(postIds);
        contentManagementService.cleanupRedisDataForComments(commentIds, postCommentCountDelta,
            commentsByPost);

        dbService.updateWithdrawnUserCommentsInDb(commentIds, postCommentCountDelta);
        dbService.updateWithdrawnUserPostsInDb(postIds);
        postRepository.unlinkMemberFromPosts(memberId);
        commentRepository.unlinkMemberFromComments(memberId);

        // 멤버 엔티티 삭제
        memberRepository.deleteById(memberId);
    }
}