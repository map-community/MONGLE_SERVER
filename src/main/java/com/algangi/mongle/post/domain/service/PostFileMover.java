package com.algangi.mongle.post.domain.service;

import java.util.Set;

public interface PostFileMover {

    Set<String> moveBulkTempToPermanent(String postId, Set<String> tempKey);

}
