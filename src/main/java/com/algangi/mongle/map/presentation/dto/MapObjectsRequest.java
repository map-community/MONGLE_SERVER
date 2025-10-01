package com.algangi.mongle.map.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record MapObjectsRequest(
    @NotNull(message = "swLat은 필수값입니다.") Double swLat,
    @NotNull(message = "swLng은 필수값입니다.") Double swLng,
    @NotNull(message = "neLat은 필수값입니다.") Double neLat,
    @NotNull(message = "neLng은 필수값입니다.") Double neLng,
    String memberId
) {

}
