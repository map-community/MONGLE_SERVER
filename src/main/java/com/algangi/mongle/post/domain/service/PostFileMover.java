package com.algangi.mongle.post.domain.service;

import java.util.List;

public interface PostFileMover {

    List<String> moveBulkTempToPermanent(String postId, List<String> tempKey);

}
