package com.algangi.mongle.post.application.service;

import java.util.List;
import java.util.Set;

import com.algangi.mongle.post.domain.model.PostFile;

public interface PostFileCommitService {

    List<PostFile> commit(String postId, Set<String> temporaryFileKeys);
}
