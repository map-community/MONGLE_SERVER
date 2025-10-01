package com.algangi.mongle.reaction.application.event;

import com.algangi.mongle.reaction.domain.model.ReactionType;
import com.algangi.mongle.reaction.domain.model.TargetType;

public record ReactionUpdatedEvent(
        String memberId,
        String targetId,
        TargetType targetType,
        ReactionType reactionType
) {

}