package com.algangi.mongle.post.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.algangi.mongle.file.application.service.FileService;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.global.exception.AwsErrorCode;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostFile;
import com.algangi.mongle.post.domain.model.PostStatus;
import com.algangi.mongle.post.exception.PostErrorCode;

@ExtendWith(MockitoExtension.class)
class PostFileCreatedEventListenerTest {

    private static final String POST_ID = "test-post-id";
    @InjectMocks
    private PostFileCreatedEventListener postFileCreatedEventListener;
    @Mock
    private FileService fileService;
    @Mock
    private PostFinder postFinder;

    // @Spy를 사용하여 실제 Post 객체의 메서드 호출을 감지합니다.
    @Spy
    private Post post = Post.createStandalone(null, "s2-token", "content",
        "author");

    @BeforeEach
    void setUp() {
        // 모든 테스트에서 postFinder가 spy 객체를 반환하도록 설정
        lenient().when(postFinder.getPostOrThrow(POST_ID)).thenReturn(post);
    }

    @Nested
    @DisplayName("파일 커밋 이벤트 핸들링 성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("첨부 파일이 있는 경우 파일 커밋 후 게시물을 Active 상태로 변경")
        void handleFileCommit_WithFiles_CommitsFilesAndMarksPostAsActive() {
            // given
            List<String> fileKeys = List.of("posts/key1.jpg", "posts/key2.png");
            PostFileCreatedEvent event = new PostFileCreatedEvent(POST_ID, fileKeys);

            // when
            postFileCreatedEventListener.handleFileCommit(event);

            // then
            ArgumentCaptor<List<PostFile>> postFilesCaptor = ArgumentCaptor.forClass(List.class);

            assertAll(
                // 1. FileService의 commitFiles가 올바른 fileKeys로 호출되었는지 검증
                () -> verify(fileService, times(1)).commitFiles(fileKeys),
                // 2. Post 객체에 PostFile이 추가되었는지 검증
                () -> verify(post, times(1)).addPostFiles(postFilesCaptor.capture()),
                // 3. Post가 Active 상태로 변경되었는지 검증
                () -> verify(post, times(1)).markAsActive()
            );
            
            List<PostFile> capturedFiles = postFilesCaptor.getValue();
            assertThat(capturedFiles).hasSize(2);
            assertThat(capturedFiles.get(0).getFileKey()).isEqualTo("posts/key1.jpg");
            assertThat(capturedFiles.get(1).getFileKey()).isEqualTo("posts/key2.png");
            assertEquals(PostStatus.ACTIVE, post.getStatus());
        }

        @Test
        @DisplayName("첨부 파일이 없는 경우 파일 커밋 없이 게시물만 Active 상태로 변경")
        void handleFileCommit_WithoutFiles_MarksPostAsActive() {
            // given
            List<String> emptyFileKeys = List.of();
            PostFileCreatedEvent event = new PostFileCreatedEvent(POST_ID, emptyFileKeys);

            // when
            postFileCreatedEventListener.handleFileCommit(event);

            // then
            assertAll(
                // 1. 파일이 없으므로 commitFiles는 호출되지 않아야 함
                () -> verify(fileService, never()).commitFiles(any()),
                // 2. 파일이 없으므로 addPostFiles는 호출되지 않아야 함
                () -> verify(post, never()).addPostFiles(any()),
                // 3. Post는 Active 상태로 변경되어야 함
                () -> verify(post, times(1)).markAsActive()
            );
            assertEquals(PostStatus.ACTIVE, post.getStatus());
        }
    }

    @Nested
    @DisplayName("파일 커밋 이벤트 핸들링 실패 케이스")
    class FailureCases {

        @Test
        @DisplayName("S3 태그 변경 중 오류가 발생하면 ApplicationException을 던지고 상태 변경을 하지 않음")
        void handleFileCommit_WhenTaggingFails_ThrowsExceptionAndDoesNotChangeStatus() {
            // given
            List<String> fileKeys = List.of("posts/key1.jpg");
            PostFileCreatedEvent event = new PostFileCreatedEvent(POST_ID, fileKeys);

            doThrow(new ApplicationException(AwsErrorCode.S3_FILE_TAGGING_FAILED))
                .when(fileService).commitFiles(fileKeys);

            // when & then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                postFileCreatedEventListener.handleFileCommit(event);
            });

            assertEquals(AwsErrorCode.S3_FILE_TAGGING_FAILED, exception.getErrorCode());

            verify(post, never()).addPostFiles(any());
            verify(post, never()).markAsActive();
        }

        @Test
        @DisplayName("Post를 찾을 수 없을 경우 예외를 던지고 파일 관련 작업을 수행하지 않음")
        void handleFileCommit_WhenPostNotFound_ThrowsException() {
            // given
            PostFileCreatedEvent event = new PostFileCreatedEvent(POST_ID,
                List.of("posts/key1.jpg"));
            doThrow(new ApplicationException(PostErrorCode.POST_NOT_FOUND)).when(postFinder)
                .getPostOrThrow(POST_ID);

            // when & then
            assertThrows(ApplicationException.class, () -> {
                postFileCreatedEventListener.handleFileCommit(event);
            });

            verify(fileService, never()).commitFiles(any());
            verify(post, never()).addPostFiles(any());
            verify(post, never()).markAsActive();
        }
    }
}