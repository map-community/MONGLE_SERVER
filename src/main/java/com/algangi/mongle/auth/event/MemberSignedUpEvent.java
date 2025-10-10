package com.algangi.mongle.auth.event;

public record MemberSignedUpEvent(
    String memberId,
    String temporaryProfileImageKey
) {

}
