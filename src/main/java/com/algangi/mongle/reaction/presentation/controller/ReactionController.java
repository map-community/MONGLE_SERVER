package com.algangi.mongle.reaction.presentation.controller;

import com.algangi.mongle.global.dto.ApiResponse;
import com.algangi.mongle.reaction.application.service.ReactionApplicationService;
import com.algangi.mongle.reaction.presentation.dto.ReactionRequest;
import com.algangi.mongle.reaction.presentation.dto.ReactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionApplicationService reactionApplicationService;

    @PostMapping("/{targetType}/{targetId}/reaction")
    public ResponseEntity<ApiResponse<ReactionResponse>> updateReaction(
            @PathVariable String targetType,
            @PathVariable String targetId,
            @RequestBody ReactionRequest request,
            @RequestParam String memberId
    ) {
        ReactionResponse result = reactionApplicationService.updateReaction(
                targetType,
                targetId,
                memberId,
                request.reactionType()
        );

        return ResponseEntity.ok(ApiResponse.success(result));
    }

}