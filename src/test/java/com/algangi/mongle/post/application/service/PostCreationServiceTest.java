package com.algangi.mongle.post.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.ArgumentCaptor;
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
import com.algangi.mongle.member.application.service.MemberFinder;
import com.algangi.mongle.member.domain.model.Member;
import com.algangi.mongle.member.exception.MemberErrorCode;
import com.algangi.mongle.post.domain.model.Location;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.repository.PostRepository;
import com.algangi.mongle.post.domain.service.LocationRandomizer;
import com.algangi.mongle.post.event.PostFileCreatedEvent;
import com.algangi.mongle.post.presentation.dto.PostCreateRequest;
import com.algangi.mongle.post.presentation.dto.PostCreateResponse;
import com.algangi.mongle.staticCloud.domain.model.StaticCloud;
import com.algangi.mongle.staticCloud.repository.StaticCloudRepository;

@ExtendWith(MockitoExtension.class)
class PostCreationServiceTest {

    private static final String AUTHOR_ID = "test-author-id";
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
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private MemberFinder memberFinder;
    @Mock
    private LocationRandomizer locationRandomizer; // ADDED
    @Mock
    private CellService cellService;
    private PostCreateRequest defaultRequest;
    private Member activeMember;
    private Location originalLocation;

    @BeforeEach
    void setUp() {
        originalLocation = Location.create(35.8714, 128.6014);
        defaultRequest = new PostCreateRequest(originalLocation.getLatitude(),
            originalLocation.getLongitude(),
            "게시물 내용", List.of("file1.jpg"), false);

        activeMember = Member.createUser("test@test.com", "{bcrypt}password", "tester", null);
        ReflectionTestUtils.setField(activeMember, "memberId", AUTHOR_ID);

        lenient().when(memberFinder.getMemberOrThrow(AUTHOR_ID)).thenReturn(activeMember);
        lenient().when(postRepository.save(any(Post.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    @DisplayName("게시물 생성 성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("정적 구름이 존재하는 셀에 게시물 생성 (랜덤화 비활성)")
        void createPost_InStaticCloud_Success() {
            // given
            StaticCloud mockStaticCloud = StaticCloud.createStaticCloud("IT 5호관", 35.8, 128.6,
                Set.of(S2_TOKEN_ID));
            ReflectionTestUtils.setField(mockStaticCloud, "id", 1L);

            when(cellService.generateS2TokenIdFrom(originalLocation.getLatitude(),
                originalLocation.getLongitude()))
                .thenReturn(S2_TOKEN_ID);
            when(staticCloudRepository.findByS2TokenId(S2_TOKEN_ID))
                .thenReturn(Optional.of(mockStaticCloud));

            // when
            PostCreateResponse response = postCreationService.createPost(defaultRequest, AUTHOR_ID);

            // then
            assertAll(
                () -> assertThat(response.staticCloudId()).isEqualTo(mockStaticCloud.getId()),
                () -> assertThat(response.dynamicCloudId()).isNull(),
                () -> verify(locationRandomizer, never()).randomize(any()),
                () -> verify(eventPublisher, times(1)).publishEvent(any(PostFileCreatedEvent.class))
            );
        }

        @Test
        @DisplayName("동적 구름이 존재하는 셀에 게시물 생성")
        void createPost_InExistingDynamicCloud_Success() {
            // given
            DynamicCloud mockDynamicCloud = DynamicCloud.create(Set.of(S2_TOKEN_ID));
            ReflectionTestUtils.setField(mockDynamicCloud, "id", 10L);

            when(cellService.generateS2TokenIdFrom(originalLocation.getLatitude(),
                originalLocation.getLongitude()))
                .thenReturn(S2_TOKEN_ID);
            when(staticCloudRepository.findByS2TokenId(S2_TOKEN_ID)).thenReturn(Optional.empty());
            when(dynamicCloudRepository.findActiveByS2TokenId(S2_TOKEN_ID))
                .thenReturn(Optional.of(mockDynamicCloud));

            // when
            PostCreateResponse response = postCreationService.createPost(defaultRequest, AUTHOR_ID);

            // then
            assertAll(
                () -> assertThat(response.dynamicCloudId()).isEqualTo(mockDynamicCloud.getId()),
                () -> assertThat(response.staticCloudId()).isNull(),
                () -> verify(locationRandomizer, never()).randomize(any()),
                () -> verify(eventPublisher, times(1)).publishEvent(any(PostFileCreatedEvent.class))
            );
        }

        @Test
        @DisplayName("새 '알갱이' 게시물 생성 (동적 구름 생성 조건 미충족)")
        void createPost_Standalone_Success() {
            // given
            when(cellService.generateS2TokenIdFrom(originalLocation.getLatitude(),
                originalLocation.getLongitude()))
                .thenReturn(S2_TOKEN_ID);
            when(staticCloudRepository.findByS2TokenId(S2_TOKEN_ID)).thenReturn(Optional.empty());
            when(dynamicCloudRepository.findActiveByS2TokenId(S2_TOKEN_ID)).thenReturn(
                Optional.empty());

            Post existingPost = Post.createStandalone(Location.create(35.0, 128.0), S2_TOKEN_ID,
                "content", "author");
            when(postRepository.findByS2TokenIdWithLock(S2_TOKEN_ID)).thenReturn(
                List.of(existingPost));

            // when
            PostCreateResponse response = postCreationService.createPost(defaultRequest, AUTHOR_ID);

            // then
            assertAll(
                () -> assertThat(response.dynamicCloudId()).isNull(),
                () -> assertThat(response.staticCloudId()).isNull(),
                () -> verify(locationRandomizer, never()).randomize(any()),
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
            ReflectionTestUtils.setField(newDynamicCloud, "id", 11L);

            when(cellService.generateS2TokenIdFrom(originalLocation.getLatitude(),
                originalLocation.getLongitude()))
                .thenReturn(S2_TOKEN_ID);
            when(staticCloudRepository.findByS2TokenId(S2_TOKEN_ID)).thenReturn(Optional.empty());
            when(dynamicCloudRepository.findActiveByS2TokenId(S2_TOKEN_ID)).thenReturn(
                Optional.empty());

            Post post1 = Post.createStandalone(Location.create(35.0, 128.0), S2_TOKEN_ID, "c1",
                "a1");
            Post post2 = Post.createStandalone(Location.create(35.0, 128.0), S2_TOKEN_ID, "c2",
                "a2");
            List<Post> existingPosts = List.of(post1, post2);

            when(postRepository.findByS2TokenIdWithLock(S2_TOKEN_ID)).thenReturn(existingPosts);
            when(dynamicCloudFormationService.createDynamicCloudAndMergeIfNeeded(S2_TOKEN_ID,
                existingPosts))
                .thenReturn(newDynamicCloud);

            // when
            PostCreateResponse response = postCreationService.createPost(defaultRequest, AUTHOR_ID);

            // then
            assertAll(
                () -> assertThat(response.dynamicCloudId()).isEqualTo(newDynamicCloud.getId()),
                () -> verify(locationRandomizer, never()).randomize(any()),
                () -> verify(dynamicCloudFormationService, times(1))
                    .createDynamicCloudAndMergeIfNeeded(S2_TOKEN_ID, existingPosts)
            );
        }

        @Test
        @DisplayName("랜덤 위치 옵션 활성화 시 LocationRandomizer 호출")
        void createPost_WithRandomLocation_Success() {
            // given
            PostCreateRequest randomRequest = new PostCreateRequest(
                originalLocation.getLatitude(), originalLocation.getLongitude(),
                "랜덤 위치 게시물", Collections.emptyList(), true);

            Location randomizedLocation = Location.create(35.8715, 128.6015);
            String randomizedTokenId = "89c259c5";

            // 1. 원본 위치로 S2 토큰 생성 -> 정적 구름 없음
            when(cellService.generateS2TokenIdFrom(originalLocation.getLatitude(),
                originalLocation.getLongitude()))
                .thenReturn(S2_TOKEN_ID);
            when(staticCloudRepository.findByS2TokenId(S2_TOKEN_ID)).thenReturn(Optional.empty());

            // 2. 위치 랜덤화 수행
            when(locationRandomizer.randomize(any(Location.class))).thenReturn(randomizedLocation);

            // 3. 랜덤화된 위치로 최종 S2 토큰 생성
            when(cellService.generateS2TokenIdFrom(randomizedLocation.getLatitude(),
                randomizedLocation.getLongitude()))
                .thenReturn(randomizedTokenId);

            when(dynamicCloudRepository.findActiveByS2TokenId(randomizedTokenId)).thenReturn(
                Optional.empty());
            when(postRepository.findByS2TokenIdWithLock(randomizedTokenId)).thenReturn(
                Collections.emptyList());

            // when
            postCreationService.createPost(randomRequest, AUTHOR_ID);

            // then
            ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
            verify(postRepository).save(postCaptor.capture());
            Post savedPost = postCaptor.getValue();

            assertAll(
                () -> verify(locationRandomizer, times(1)).randomize(any(Location.class)),
                () -> assertThat(savedPost.getLocation().getLatitude()).isEqualTo(
                    randomizedLocation.getLatitude()),
                () -> assertThat(savedPost.getLocation().getLongitude()).isEqualTo(
                    randomizedLocation.getLongitude()),
                () -> assertThat(savedPost.getS2TokenId()).isEqualTo(randomizedTokenId)
            );
        }

        @Test
        @DisplayName("비관적 락 이후 동적 구름이 생성된 경우 (Concurrency Gap 방지)")
        void createPost_HandleConcurrencyGap_Success() {
            // given
            DynamicCloud cloudCreatedDuringLock = DynamicCloud.create(Set.of(S2_TOKEN_ID));
            ReflectionTestUtils.setField(cloudCreatedDuringLock, "id", 12L);

            when(cellService.generateS2TokenIdFrom(originalLocation.getLatitude(),
                originalLocation.getLongitude()))
                .thenReturn(S2_TOKEN_ID);
            when(staticCloudRepository.findByS2TokenId(S2_TOKEN_ID)).thenReturn(Optional.empty());
            when(dynamicCloudRepository.findActiveByS2TokenId(S2_TOKEN_ID))
                .thenReturn(Optional.empty()) // Before lock
                .thenReturn(Optional.of(cloudCreatedDuringLock)); // After lock
            when(postRepository.findByS2TokenIdWithLock(S2_TOKEN_ID)).thenReturn(
                Collections.emptyList());

            // when
            PostCreateResponse response = postCreationService.createPost(defaultRequest, AUTHOR_ID);

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
                postCreationService.createPost(defaultRequest, AUTHOR_ID);
            });

            assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_IS_BANNED);
        }
    }
}