package com.algangi.mongle.member.application.event;

import com.algangi.mongle.stats.application.dto.ReactionCleanupDto;

import java.util.List;
import java.util.Map;

public record MemberWithdrawnEvent(
        String memberId,
        List<String> postIds,
        List<String> commentIds,
        List<ReactionCleanupDto> reactions,
        Map<String, Long> postCommentCountDelta,
        Map<String, List<String>> commentsByPost
) {}
