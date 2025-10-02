package com.algangi.mongle.member.application.service;

import com.algangi.mongle.comment.domain.model.Comment;
import com.algangi.mongle.comment.domain.model.CommentStatus;
import com.algangi.mongle.comment.domain.repository.CommentRepository;
import com.algangi.mongle.post.domain.model.PostStatus;
import com.algangi.mongle.post.domain.repository.PostRepository;
import com.algangi.mongle.reaction.domain.model.TargetType;
import com.algangi.mongle.reaction.domain.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentManagementService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ReactionRepository reactionRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Async("bannedUserContentTaskExecutor")
    @Transactional
    public void processCommentsOfBannedUser(String bannedMemberId) {
        log.info("Starting async comment processing for banned user: {}", bannedMemberId);

        // 1. 처리 대상 댓글 조회
        List<Comment> commentsToProcess = findCommentsByBannedUser(bannedMemberId);
        if (commentsToProcess.isEmpty()) {
            log.info("No active comments to process for user: {}", bannedMemberId);
            return;
        }

        // 2. 댓글 ID 추출
        List<String> commentIds = commentsToProcess.stream()
                .map(Comment::getId)
                .toList();

        // 3. <게시글 ID, 삭제될 댓글 개수>
        Map<String, Long> postCommentCountDelta = commentsToProcess.stream()
                .collect(Collectors.groupingBy(comment -> comment.getPost().getId(),
                        Collectors.counting()));

        // 4. <게시글 ID, 삭제될 댓글 ID 목록>
        Map<String, List<String>> commentsByPost = commentsToProcess.stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getPost().getId(),
                        Collectors.mapping(Comment::getId, Collectors.toList())
                ));

        // 5. DB 업데이트
        commentRepository.updateStatusForIds(commentIds, CommentStatus.DELETED_BY_ADMIN);
        postCommentCountDelta.forEach(postRepository::decrementCommentCount);
        reactionRepository.deleteAllByTargetTypeAndTargetIdIn(TargetType.COMMENT, commentIds);

        // 6. Redis 업데이트
        cleanupRedisDataForComments(commentIds, postCommentCountDelta, commentsByPost);
        log.info("Finished async comment processing for banned user: {}", bannedMemberId);
    }

    private List<Comment> findCommentsByBannedUser(String bannedMemberId) {
        return commentRepository.findAllByMemberIdAndPostStatusIn(
                bannedMemberId, List.of(PostStatus.UPLOADING, PostStatus.ACTIVE)
        );
    }

    private void cleanupRedisDataForComments(List<String> commentIds, Map<String, Long> postCommentCountDelta, Map<String, List<String>> commentsByPost) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            // 1. 댓글 관련 키 일괄 삭제
            if (!commentIds.isEmpty()) {
                byte[][] keysToDelete = commentIds.stream()
                        .flatMap(commentId -> Stream.of(
                                ("likes::comment::" + commentId).getBytes(),
                                ("dislikes::comment::" + commentId).getBytes(),
                                ("reactions::comment::" + commentId).getBytes()
                        ))
                        .toArray(byte[][]::new);
                connection.del(keysToDelete);
            }

            // 2. 게시글별 댓글 수 감소
            postCommentCountDelta.forEach((postId, count) -> {
                String commentCountKey = "comments::post::" + postId;
                connection.stringCommands().decrBy(commentCountKey.getBytes(), count);
            });

            // 3. 댓글 랭킹 정리(좋아요순)
            commentsByPost.forEach((postId, ids) -> {
                if (!ids.isEmpty()) {
                    String rankingKey = "comments_by_likes::post::" + postId;
                    byte[][] membersToDelete = ids.stream().map(String::getBytes).toArray(byte[][]::new);
                    connection.zSetCommands().zRem(rankingKey.getBytes(), membersToDelete);
                }
            });

            return null;
        });
    }

    private void cleanupRedisDataForPosts(List<String> postIds) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            if (!postIds.isEmpty()) {
                byte[][] keysToDelete = postIds.stream()
                        .flatMap(postId -> Stream.of(
                                ("views::post::" + postId).getBytes(),
                                ("comments::post::" + postId).getBytes(),
                                ("likes::post::" + postId).getBytes(),
                                ("dislikes::post::" + postId).getBytes(),
                                ("reactions::post::" + postId).getBytes(),
                                ("comments_by_likes::post::" + postId).getBytes()
                        ))
                        .toArray(byte[][]::new);
                connection.keyCommands().del(keysToDelete);
            }
            return null;
        });
    }
}