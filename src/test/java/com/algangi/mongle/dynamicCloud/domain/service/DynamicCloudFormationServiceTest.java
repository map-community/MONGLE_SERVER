package com.algangi.mongle.dynamicCloud.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.algangi.mongle.dynamicCloud.domain.model.DynamicCloud;
import com.algangi.mongle.dynamicCloud.domain.model.DynamicCloudStatus;
import com.algangi.mongle.dynamicCloud.domain.repository.DynamicCloudRepository;
import com.algangi.mongle.global.domain.service.CellService;
import com.algangi.mongle.post.domain.model.Post;
import com.algangi.mongle.post.domain.repository.PostRepository;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class DynamicCloudFormationServiceTest {

    private static final String S2_TOKEN_ID = "cell_A";
    @InjectMocks
    private DynamicCloudFormationService dynamicCloudFormationService;
    @Mock
    private DynamicCloudRepository dynamicCloudRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CellService cellService;

    @BeforeEach
    void setUp() {
        // save() 메서드가 받은 객체를 그대로 반환하도록 설정 (ID가 부여된 것처럼 시뮬레이션)
        when(dynamicCloudRepository.save(any(DynamicCloud.class))).thenAnswer(invocation -> {
            DynamicCloud cloud = invocation.getArgument(0);
            if (cloud.getId() == null) {
                // ID가 없는 새 구름이면 ID를 부여해줌
                ReflectionTestUtils.setField(cloud, "id", 100L);
            }
            return cloud;
        });
    }

    @Nested
    @DisplayName("동적 구름 생성 및 병합 성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("인접 구름이 없을 경우 새 동적 구름만 생성")
        void createDynamicCloud_WhenNoAdjacentClouds_Success() {
            // given
            Set<String> adjacentCells = Set.of("cell_B", "cell_C");
            when(cellService.getAdjacentCells(S2_TOKEN_ID)).thenReturn(adjacentCells);
            // 인접 셀들에 ACTIVE 동적 구름이 없다고 설정
            when(dynamicCloudRepository.findActiveCloudsInCells(new ArrayList<>(adjacentCells)))
                .thenReturn(Collections.emptyList());

            Post newPost = Post.createStandalone("newPostId", null, S2_TOKEN_ID, "content",
                "author");
            List<Post> existingPostsInCell = new ArrayList<>(List.of(newPost));

            // when
            DynamicCloud resultCloud = dynamicCloudFormationService.createDynamicCloudAndMergeIfNeeded(
                S2_TOKEN_ID, existingPostsInCell);

            // then
            assertAll(
                () -> assertThat(resultCloud).isNotNull(),
                () -> assertThat(resultCloud.getS2TokenIds()).containsExactly(S2_TOKEN_ID),
                () -> assertThat(newPost.getDynamicCloudId()).isEqualTo(resultCloud.getId()),
                () -> verify(dynamicCloudRepository, times(1)).save(any(DynamicCloud.class)),
                () -> verify(postRepository, times(1)).saveAll(existingPostsInCell),
                // 병합 로직 관련 메서드는 호출되지 않아야 함
                () -> verify(postRepository, never()).findByDynamicCloudIdIn(anyList())
            );
        }

        @Test
        @DisplayName("인접 구름이 존재할 경우 가장 오래된 구름으로 병합")
        void createDynamicCloud_WhenAdjacentCloudsExist_MergesToOldest() {
            // given
            // @Spy: 실제 객체를 사용하되, 일부 메서드는 Mocking. 여기서는 실제 mergeWith 로직을 테스트하기 위해 사용
            DynamicCloud oldestCloud = DynamicCloud.create(Set.of("cell_B"));
            ReflectionTestUtils.setField(oldestCloud, "id", 1L);
            ReflectionTestUtils.setField(oldestCloud, "createdDate",
                LocalDateTime.now().minusDays(2));

            DynamicCloud youngerCloud = DynamicCloud.create(Set.of("cell_C"));
            ReflectionTestUtils.setField(youngerCloud, "id", 2L);
            ReflectionTestUtils.setField(youngerCloud, "createdDate",
                LocalDateTime.now().minusDays(1));

            Set<String> adjacentCells = Set.of("cell_B", "cell_C");
            when(cellService.getAdjacentCells(S2_TOKEN_ID)).thenReturn(adjacentCells);
            when(dynamicCloudRepository.findActiveCloudsInCells(new ArrayList<>(adjacentCells)))
                .thenReturn(List.of(oldestCloud, youngerCloud));

            // 병합될 구름(youngerCloud)에 속한 게시물들
            Post postToReassign = Post.createInDynamicCloud("p1", null, "cell_C", "c1", "a1", 2L);
            when(postRepository.findByDynamicCloudIdIn(List.of(2L))).thenReturn(
                List.of(postToReassign));

            // 새로 생성되는 셀(cell_A)에 속한 게시물
            Post newPost = Post.createStandalone("p2", null, S2_TOKEN_ID, "c2", "a2");
            List<Post> existingPostsInCell = List.of(newPost);

            // when
            DynamicCloud resultCloud = dynamicCloudFormationService.createDynamicCloudAndMergeIfNeeded(
                S2_TOKEN_ID, existingPostsInCell);

            // then
            assertAll(
                // 최종 반환된 구름은 가장 오래된 구름이어야 함
                () -> assertThat(resultCloud.getId()).isEqualTo(oldestCloud.getId()),
                // 가장 오래된 구름은 모든 셀 정보를 포함해야 함 (cell_A, cell_B, cell_C)
                () -> assertThat(resultCloud.getS2TokenIds()).containsExactlyInAnyOrder(S2_TOKEN_ID,
                    "cell_B", "cell_C"),
                // 병합된 구름의 상태는 MERGED로 변경되어야 함
                () -> assertThat(youngerCloud.getStatus()).isEqualTo(DynamicCloudStatus.MERGED),
                // 재할당될 게시물의 dynamicCloudId가 가장 오래된 구름의 ID로 변경되어야 함
                () -> assertThat(postToReassign.getDynamicCloudId()).isEqualTo(oldestCloud.getId()),
                // 새로 추가된 게시물의 dynamicCloudId도 가장 오래된 구름의 ID로 변경되어야 함
                () -> assertThat(newPost.getDynamicCloudId()).isEqualTo(oldestCloud.getId()),
                // 재할당될 게시물 목록이 저장되어야 함
                () -> verify(postRepository, times(1)).saveAll(List.of(postToReassign)),
                // 새로 추가된 게시물 목록이 저장되어야 함
                () -> verify(postRepository, times(1)).saveAll(existingPostsInCell)
            );
        }

        @Test
        @DisplayName("엣지 케이스: 할당할 기존 게시물이 없어도 구름 생성/병합은 정상 동작")
        void createDynamicCloud_WhenNoExistingPosts_ShouldWork() {
            // given
            Set<String> adjacentCells = Set.of("cell_B");
            when(cellService.getAdjacentCells(S2_TOKEN_ID)).thenReturn(adjacentCells);
            when(dynamicCloudRepository.findActiveCloudsInCells(new ArrayList<>(adjacentCells)))
                .thenReturn(Collections.emptyList());

            // 할당할 게시물이 비어있는 리스트
            List<Post> existingPostsInCell = Collections.emptyList();

            // when
            DynamicCloud resultCloud = dynamicCloudFormationService.createDynamicCloudAndMergeIfNeeded(
                S2_TOKEN_ID, existingPostsInCell);

            // then
            assertAll(
                () -> assertThat(resultCloud).isNotNull(),
                () -> assertThat(resultCloud.getS2TokenIds()).containsExactly(S2_TOKEN_ID),
                // existingPostsInCell이 비어있으므로 saveAll은 호출되지 않아야 함
                () -> verify(postRepository, never()).saveAll(existingPostsInCell)
            );
        }
    }
}