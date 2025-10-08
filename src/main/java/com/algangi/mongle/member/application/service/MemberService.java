package com.algangi.mongle.member.application.service;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.repository.CommentRepository;
import com.algangi.mongle.member.application.event.MemberWithdrawnEvent;
import com.algangi.mongle.post.domain.repository.PostRepository;
import com.algangi.mongle.reaction.infrastructure.persistence.ReactionRepositoryCustom;
import com.algangi.mongle.stats.application.dto.ReactionCleanupDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final ApplicationEventPublisher eventPublisher;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ReactionRepositoryCustom reactionRepositoryCustom;

    @Transactional
    public void withdrawMember(String memberId) {
        // 1. 앞으로 처리해야 할 ID 및 DTO 목록을 미리 조회
        List<String> postIdsToProcess = postRepository.findAllIdsByMemberId(memberId);
        List<ReactionCleanupDto> reactionsToProcess = reactionRepositoryCustom.findAllReactionCleanupData(memberId);

        List<Comment> commentsToProcess = commentRepository.findAllByMember_MemberId(memberId);
        List<String> commentIdsToProcess = commentsToProcess.stream().map(Comment::getId).toList();

        // 2. Redis 정리에 필요한 Map 가공
        Map<String, Long> postCommentCountDelta = commentsToProcess.stream()
                .collect(Collectors.groupingBy(comment -> comment.getPost().getId(), Collectors.counting()));

        Map<String, List<String>> commentsByPost = commentsToProcess.stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getPost().getId(),
                        Collectors.mapping(Comment::getId, Collectors.toList())
                ));

        // 3. 이벤트 발행
        MemberWithdrawnEvent event = new MemberWithdrawnEvent(
                memberId,
                postIdsToProcess,
                commentIdsToProcess,
                reactionsToProcess,
                postCommentCountDelta,
                commentsByPost
        );
        eventPublisher.publishEvent(event);
    }
}