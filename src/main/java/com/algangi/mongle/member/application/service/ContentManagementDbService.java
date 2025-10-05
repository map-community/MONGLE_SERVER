package com.algangi.mongle.member.application.service;

import com.algangi.mongle.comment.domain.model.CommentStatus;
import com.algangi.mongle.comment.domain.repository.CommentRepository;
import com.algangi.mongle.post.domain.model.PostStatus;
import com.algangi.mongle.post.domain.repository.PostRepository;
import com.algangi.mongle.reaction.domain.model.TargetType;
import com.algangi.mongle.reaction.domain.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContentManagementDbService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ReactionRepository reactionRepository;

    @Transactional
    public void updateBannedUserCommentsInDb(List<String> commentIds,
        Map<String, Long> postCommentCountDelta) {
        commentRepository.updateStatusForIds(commentIds, CommentStatus.DELETED_BY_ADMIN);
        postCommentCountDelta.forEach(postRepository::decrementCommentCount);
        reactionRepository.deleteAllByTargetTypeAndTargetIdIn(TargetType.COMMENT, commentIds);
    }

    @Transactional
    public void updateBannedUserPostsInDb(List<String> postIds) {
        postRepository.updateStatusForIds(postIds, PostStatus.DELETED_BY_ADMIN);
        reactionRepository.deleteAllByTargetTypeAndTargetIdIn(TargetType.POST, postIds);
    }
}