package com.algangi.mongle.post.domain.service;

import java.util.Set;

public interface PostFileCommitValidateService {

    void validateTemporaryFiles(Set<String> tempKey);

}
