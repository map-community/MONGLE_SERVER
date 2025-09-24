package com.algangi.mongle.global.domain;

import java.util.Set;

public class FileUploadConstants {

    public static final String POST_IMAGE_DIR = "posts/images/";
    public static final String POST_VIDEO_DIR = "posts/videos/";

    public static final long MAX_IMAGE_SIZE_MB = 10 * 1024 * 1024;
    public static final long MAX_TOTAL_IMAGE_SIZE_MB = 50 * 1024 * 1024;
    public static final long MAX_VIDEO_SIZE_MB = 100 * 1024 * 1024;
    public static final int MAX_FILE_COUNT = 10;
    public static final int MAX_VIDEO_COUNT = 1;

    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    public static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of("mp4", "mov", "avi");

}
