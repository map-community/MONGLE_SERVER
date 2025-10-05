package com.algangi.mongle.post.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import com.algangi.mongle.dynamicCloud.domain.model.DynamicCloud;
import com.algangi.mongle.dynamicCloud.domain.repository.DynamicCloudRepository;
import com.algangi.mongle.dynamicCloud.domain.service.DynamicCloudFormationService;
import com.algangi.mongle.global.domain.service.CellService;
import com.algangi.mongle.global.exception.ApplicationException;
import com.algangi.mongle.member.domain.Member;
import com.algangi.mongle.member.exception.MemberErrorCode;
import com.algangi.mongle.member.service.MemberFinder;
import com.algangi.mongle.post.domain.model.Location;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.repository.PostRepository;
import com.algangi.mongle.post.domain.service.PostFileCommitValidationService;
import com.algangi.mongle.post.domain.service.PostIdService;
import com.algangi.mongle.post.event.PostFileCreatedEvent;
import com.algangi.mongle.post.presentation.dto.PostCreateRequest;
import com.algangi.mongle.post.presentation.dto.PostResponse;
import com.algangi.mongle.staticCloud.domain.model.StaticCloud;
import com.algangi.mongle.staticCloud.repository.StaticCloudRepository;

@ExtendWith(MockitoExtension.class)
class PostCreationServiceTest {

    private static final String AUTHOR_ID = "test-author-id";
    private static final String POST_ID = "01ARZ3NDEKTSV4RRFFQ69G5FAV";
    private static final String S2_TOKEN_ID = "89c259c4";
    @InjectMocks
    private PostCreationService postCreationService;
    @Mock
    private StaticCloudRepository staticCloudRepository;
    @Mock
    private DynamicCloudRepository dynamicCloudRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private DynamicCloudFormationService dynamicCloudFormationService;
    @Mock
    private PostFileCommitValidationService postFileCommitValidationService;
    @Mock
    private PostIdService postIdService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private CellService cellService;
    @Mock
    private MemberFinder memberFinder;
    private PostCreateRequest request;
    private Member activeMember;

    @BeforeEach
    void setUp() {
        request = new PostCreateRequest(35.8714, 128.6014, "게시물 내용", List.of("file1.jpg"));

        activeMember = Member.createUser("test@test.com", "{bcrypt}password", "tester", null);
        ReflectionTestUtils.setField(activeMember, "memberId", AUTHOR_ID);

        // 공통 Mocking 설정
        when(memberFinder.getMemberOrThrow(AUTHOR_ID)).thenReturn(activeMember);
        lenient().when(postIdService.createId()).thenReturn(POST_ID);
        lenient().when(cellService.generateS2TokenIdFrom(anyDouble(), anyDouble()))
            .thenReturn(S2_TOKEN_ID);
        lenient().when(postRepository.save(any(Post.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    @DisplayName("게시물 생성 성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("정적 구름이 존재하는 셀에 게시물 생성")
        void createPost_InStaticCloud_Success() {
            // given
            StaticCloud mockStaticCloud = StaticCloud.createStaticCloud("IT 5호관", 35.8, 128.6,
                Set.of(S2_TOKEN_ID));
            ReflectionTestUtils.setField(mockStaticCloud, "id", 1L);
            when(staticCloudRepository.findByS2TokenId(S2_TOKEN_ID)).thenReturn(
                Optional.of(mockStaticCloud));

            // when
            PostResponse response = postCreationService.createPost(request, AUTHOR_ID);

            // then
            assertAll(
                () -> assertThat(response.staticCloudId()).isEqualTo(mockStaticCloud.getId()),
                () -> assertThat(response.dynamicCloudId()).isNull(),
                () -> verify(eventPublisher, times(1)).publishEvent(any(PostFileCreatedEvent.class))
            );
        }

        @Test
        @DisplayName("동적 구름이 존재하는 셀에 게시물 생성")
        void createPost_InExistingDynamicCloud_Success() {
            // given
            DynamicCloud mockDynamicCloud = DynamicCloud.create(Set.of(S2_TOKEN_ID));
            ReflectionTestUtils.setField(mockDynamicCloud, "id", 10L); // ID 주입
            when(staticCloudRepository.findByS2TokenId(S2_TOKEN_ID)).thenReturn(Optional.empty());
            when(dynamicCloudRepository.findActiveByS2TokenId(S2_TOKEN_ID)).thenReturn(
                Optional.of(mockDynamicCloud));

            // when
            PostResponse response = postCreationService.createPost(request, AUTHOR_ID);

            // then
            assertAll(
                () -> assertThat(response.dynamicCloudId()).isEqualTo(mockDynamicCloud.getId()),
                () -> assertThat(response.staticCloudId()).isNull(),
                () -> verify(eventPublisher, times(1)).publishEvent(any(PostFileCreatedEvent.class))
            );
        }

        @Test
        @DisplayName("새 '알갱이' 게시물 생성 (동적 구름 생성 조건 미충족)")
        void createPost_Standalone_Success() {
            // given
            when(staticCloudRepository.findByS2TokenId(S2_TOKEN_ID)).thenReturn(Optional.empty());
            when(dynamicCloudRepository.findActiveByS2TokenId(S2_TOKEN_ID)).thenReturn(
                Optional.empty());
            Post existingPost = Post.createStandalone(Location.create(35.0, 128.0),
                S2_TOKEN_ID, "content", "author");
            when(postRepository.findByS2TokenIdWithLock(S2_TOKEN_ID)).thenReturn(
                List.of(existingPost));

            // when
            PostResponse response = postCreationService.createPost(request, AUTHOR_ID);

            // then
            assertAll(
                () -> assertThat(response.dynamicCloudId()).isNull(),
                () -> assertThat(response.staticCloudId()).isNull(),
                () -> verify(dynamicCloudFormationService,
                    never()).createDynamicCloudAndMergeIfNeeded(any(), any()),
                () -> verify(eventPublisher, times(1)).publishEvent(any(PostFileCreatedEvent.class))
            );
        }

        @Test
        @DisplayName("새 동적 구름 생성 및 게시물 할당 (조건 충족)")
        void createPost_WithNewDynamicCloud_Success() {
            // given
            DynamicCloud newDynamicCloud = DynamicCloud.create(Set.of(S2_TOKEN_ID));
            ReflectionTestUtils.setField(newDynamicCloud, "id", 11L); // ID 주입

            when(staticCloudRepository.findByS2TokenId(S2_TOKEN_ID)).thenReturn(Optional.empty());
            when(dynamicCloudRepository.findActiveByS2TokenId(S2_TOKEN_ID)).thenReturn(
                Optional.empty());

            Post post1 = Post.createStandalone(Location.create(35.0, 128.0), S2_TOKEN_ID,
                "c1", "a1");
            Post post2 = Post.createStandalone(Location.create(35.0, 128.0), S2_TOKEN_ID,
                "c2", "a2");
            List<Post> existingPosts = List.of(post1, post2);

            when(postRepository.findByS2TokenIdWithLock(S2_TOKEN_ID)).thenReturn(existingPosts);
            when(dynamicCloudFormationService.createDynamicCloudAndMergeIfNeeded(S2_TOKEN_ID,
                existingPosts))
                .thenReturn(newDynamicCloud);

            // when
            PostResponse response = postCreationService.createPost(request, AUTHOR_ID);

            // then
            assertAll(
                () -> assertThat(response.dynamicCloudId()).isEqualTo(newDynamicCloud.getId()),
                () -> verify(dynamicCloudFormationService,
                    times(1)).createDynamicCloudAndMergeIfNeeded(S2_TOKEN_ID, existingPosts)
            );
        }

        @Test
        @DisplayName("비관적 락 이후 동적 구름이 생성된 경우 (Concurrency Gap 방지)")
        void createPost_HandleConcurrencyGap_Success() {
            // given
            DynamicCloud cloudCreatedDuringLock = DynamicCloud.create(Set.of(S2_TOKEN_ID));
            ReflectionTestUtils.setField(cloudCreatedDuringLock, "id", 12L);

            when(staticCloudRepository.findByS2TokenId(S2_TOKEN_ID)).thenReturn(Optional.empty());
            when(dynamicCloudRepository.findActiveByS2TokenId(S2_TOKEN_ID))
                .thenReturn(Optional.empty()) // 락 이전
                .thenReturn(Optional.of(cloudCreatedDuringLock)); // 락 이후
            when(postRepository.findByS2TokenIdWithLock(S2_TOKEN_ID)).thenReturn(
                Collections.emptyList());

            // when
            PostResponse response = postCreationService.createPost(request, AUTHOR_ID);

            // then
            assertAll(
                () -> assertThat(response.dynamicCloudId()).isEqualTo(
                    cloudCreatedDuringLock.getId()),
                () -> verify(dynamicCloudFormationService,
                    never()).createDynamicCloudAndMergeIfNeeded(any(), any())
            );
        }
    }

    @Nested
    @DisplayName("게시물 생성 실패 케이스")
    class FailureCases {

        @Test
        @DisplayName("BANNED 상태의 사용자는 게시물 생성 불가")
        void createPost_ByBannedMember_ThrowsException() {
            // given
            Member bannedMember = Member.createUser("banned@test.com", "{bcrypt}password", "banned",
                null);
            bannedMember.ban();
            when(memberFinder.getMemberOrThrow(AUTHOR_ID)).thenReturn(bannedMember);

            // when & then
            ApplicationException exception = assertThrows(ApplicationException.class, () -> {
                postCreationService.createPost(request, AUTHOR_ID);
            });

            assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_IS_BANNED);
        }
    }
}