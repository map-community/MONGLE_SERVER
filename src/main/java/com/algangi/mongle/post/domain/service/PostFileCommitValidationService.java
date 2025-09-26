package com.algangi.mongle.post.domain.service;

import java.util.Set;

public interface PostFileCommitValidationService {

    void validateTemporaryFiles(Set<String> tempKey);

}
