package com.algangi.mongle.map.presentation.controller;

import com.algangi.mongle.global.dto.ApiResponse;
import com.algangi.mongle.map.application.service.MapQueryService;
import com.algangi.mongle.map.presentation.dto.MapObjectsRequest;
import com.algangi.mongle.map.presentation.dto.MapObjectsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final MapQueryService mapQueryService;

    @GetMapping("/objects")
    public ResponseEntity<ApiResponse<MapObjectsResponse>> getMapObjects(
        @Valid @ModelAttribute MapObjectsRequest request) {

        MapObjectsResponse response = mapQueryService.getMapObjects(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
