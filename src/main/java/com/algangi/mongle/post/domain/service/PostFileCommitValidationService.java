package com.algangi.mongle.post.domain.service;

import java.util.List;

public interface PostFileCommitValidationService {

    void validateTemporaryFiles(List<String> tempKey);

}
