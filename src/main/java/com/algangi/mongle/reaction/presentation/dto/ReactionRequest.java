package com.algangi.mongle.reaction.presentation.dto;

import com.algangi.mongle.reaction.domain.model.ReactionType;

public record ReactionRequest(
        ReactionType reactionType
) {

}