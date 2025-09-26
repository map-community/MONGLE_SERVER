package com.algangi.mongle.post.domain.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class PostIdService {

    private static final String POST_PREFIX = "post";
    private static final String ID_DELIMITER = "-";

    public String createId() {
        return POST_PREFIX + ID_DELIMITER + UUID.randomUUID().toString();
    }

}
