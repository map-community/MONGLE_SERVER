package com.algangi.mongle.auth.domain.oauth2;

import java.util.Map;

public record OAuth2UserInfo(
    String providerId,
    String email,
    String nickname,
    String profileImage,
    Map<String, Object> attributes
) {

}
