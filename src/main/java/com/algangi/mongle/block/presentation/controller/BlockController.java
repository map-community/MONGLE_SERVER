package com.algangi.mongle.block.presentation.controller;

import com.algangi.mongle.auth.infrastructure.security.authentication.CustomUserDetails;
import com.algangi.mongle.block.application.service.BlockCommandService;
import com.algangi.mongle.block.application.service.BlockQueryService;
import com.algangi.mongle.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockCommandService blockCommandService;
    private final BlockQueryService blockQueryService;


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<String>>> getMyBlockedUsers(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        List<String> blockedUserIds = blockQueryService.getBlockedUserIds(user.userId());
        return ResponseEntity.ok(ApiResponse.success(blockedUserIds));
    }

    @PostMapping("/{blockedUserId}")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @PathVariable String blockedUserId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        blockCommandService.blockUser(user.userId(), blockedUserId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{blockedUserId}")
    public ResponseEntity<ApiResponse<Void>> unblockUser(
            @PathVariable String blockedUserId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        blockCommandService.unblockUser(user.userId(), blockedUserId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}