package com.algangi.mongle.member.presentation.dto;

import java.util.Map;

public record SocialLinkStatus(
    Map<String, Boolean> linkedProviders
) {

}
