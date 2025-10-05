package com.algangi.mongle.post.event;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.global.exception.AwsErrorCode;
import com.algangi.mongle.post.application.helper.PostFinder;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.model.PostFile;
import com.algangi.mongle.post.domain.service.PostFileMover;

@ExtendWith(MockitoExtension.class)
class PostFileCreatedEventListenerTest {

    private static final String POST_ID = "test-post-id";
    @InjectMocks
    private PostFileCreatedEventListener postFileCreatedEventListener;
    @Mock
    private PostFileMover postFileMover;
    @Mock
    private PostFinder postFinder;
    @Spy
    private Post post = Post.createStandalone(null, "s2-token", "content",
        "author");

    @BeforeEach
    void setUp() {
        lenient().when(postFinder.getPostOrThrow(POST_ID)).thenReturn(post);
    }

    @Nested
    @DisplayName("파일 커밋 이벤트 핸들링 성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("첨부 파일이 있는 경우 파일 이동 후 게시물을 Active 상태로 변경")
        void handleFileCommit_WithFiles_MovesFilesAndMarksPostAsActive() {
            // given
            List<String> tempKeys = List.of("temp/key1.jpg", "temp/key2.png");
            List<String> permanentKeys = List.of("permanent/key1.jpg", "permanent/key2.png");
            PostFileCreatedEvent event = new PostFileCreatedEvent(POST_ID, tempKeys);

            when(postFileMover.moveBulkTempToPermanent(POST_ID, tempKeys)).thenReturn(
                permanentKeys);

            // when
            postFileCreatedEventListener.handleFileCommit(event);

            // then
            ArgumentCaptor<List<PostFile>> postFilesCaptor = ArgumentCaptor.forClass(List.class);

            assertAll(
                () -> verify(postFileMover, times(1)).moveBulkTempToPermanent(POST_ID, tempKeys),
                () -> verify(post, times(1)).addPostFiles(postFilesCaptor.capture()),
                () -> verify(post, times(1)).markAsActive()
            );
        }

        @Test
        @DisplayName("첨부 파일이 없는 경우 파일 이동 없이 게시물만 Active 상태로 변경")
        void handleFileCommit_WithoutFiles_MarksPostAsActive() {
            // given
            List<String> emptyTempKeys = List.of();
            PostFileCreatedEvent event = new PostFileCreatedEvent(POST_ID, emptyTempKeys);

            // when
            postFileCreatedEventListener.handleFileCommit(event);

            // then
            assertAll(
                () -> verify(postFileMover, never()).moveBulkTempToPermanent(any(), any()),
                () -> verify(post, never()).addPostFiles(any()),
                () -> verify(post, times(1)).markAsActive()
            );
        }
    }

    @Nested
    @DisplayName("파일 커밋 이벤트 핸들링 실패 케이스")
    class FailureCases {

        @Test
        @DisplayName("파일 복사(Copy) 중 오류가 발생하면 ApplicationException을 던짐")
        void handleFileCommit_WhenCopyFails_ThrowsApplicationException() {
            // given
            List<String> tempKeys = List.of("temp/key1.jpg");
            PostFileCreatedEvent event = new PostFileCreatedEvent(POST_ID, tempKeys);

            when(postFileMover.moveBulkTempToPermanent(POST_ID, tempKeys))
                .thenThrow(new ApplicationException(AwsErrorCode.S3_FILE_COPY_FAILED));

            // when & then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                postFileCreatedEventListener.handleFileCommit(event);
            });

            assertEquals(AwsErrorCode.S3_FILE_COPY_FAILED, exception.getErrorCode());
            verify(post, never()).markAsActive();
        }

        @Test
        @DisplayName("임시 파일 삭제(Delete) 중 오류가 발생하면 ApplicationException을 던짐")
        void handleFileCommit_WhenDeleteFails_ThrowsApplicationException() {
            // given
            List<String> tempKeys = List.of("temp/key1.jpg");
            PostFileCreatedEvent event = new PostFileCreatedEvent(POST_ID, tempKeys);

            when(postFileMover.moveBulkTempToPermanent(POST_ID, tempKeys))
                .thenThrow(new ApplicationException(AwsErrorCode.S3_FILE_DELETE_FAILED));

            // when & then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                postFileCreatedEventListener.handleFileCommit(event);
            });

            assertEquals(AwsErrorCode.S3_FILE_DELETE_FAILED, exception.getErrorCode());
            verify(post, never()).markAsActive();
        }


        @Test
        @DisplayName("Post를 찾을 수 없을 경우 예외를 던짐")
        void handleFileCommit_WhenPostNotFound_ThrowsException() {
            // given
            PostFileCreatedEvent event = new PostFileCreatedEvent(POST_ID,
                List.of("temp/key1.jpg"));
            // PostFinder가 예외를 던지도록 설정 (RuntimeException으로 가정)
            doThrow(new RuntimeException("Post not found")).when(postFinder)
                .getPostOrThrow(POST_ID);

            // when & then
            assertThrows(RuntimeException.class, () -> {
                postFileCreatedEventListener.handleFileCommit(event);
            });

            verify(postFileMover, never()).moveBulkTempToPermanent(any(), any());
            verify(post, never()).markAsActive();
        }
    }
}