package com.algangi.mongle.stats.application.dto;

import com.algangi.mongle.reaction.domain.model.ReactionType;
import com.algangi.mongle.reaction.domain.model.TargetType;

public record ReactionCleanupDto(
        String targetId,
        TargetType targetType,
        ReactionType reactionType,
        String postId
) {}