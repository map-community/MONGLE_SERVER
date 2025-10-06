package com.algangi.mongle.post.domain.service;

import java.util.List;

public interface PostFileHandler {

    List<String> moveBulkTempToPermanent(String postId, List<String> tempKey);

    void deletePermanentFiles(List<String> fileKeys);

}
