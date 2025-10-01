package com.algangi.mongle.block.presentation.controller;

import com.algangi.mongle.block.application.service.BlockCommandService;
import com.algangi.mongle.block.application.service.BlockQueryService;
import com.algangi.mongle.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            @RequestParam String memberId
    ) {
        List<String> blockedUserIds = blockQueryService.getBlockedUserIds(memberId);
        return ResponseEntity.ok(ApiResponse.success(blockedUserIds));
    }

    @PostMapping("/{blockedUserId}")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @PathVariable String blockedUserId,
            @RequestParam String memberId
    ) {
        blockCommandService.blockUser(memberId, blockedUserId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{blockedUserId}")
    public ResponseEntity<ApiResponse<Void>> unblockUser(
            @PathVariable String blockedUserId,
            @RequestParam String memberId
    ) {
        blockCommandService.unblockUser(memberId, blockedUserId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
