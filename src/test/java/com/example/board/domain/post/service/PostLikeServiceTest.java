package com.example.board.domain.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.board.domain.post.entity.LikeType;
import com.example.board.domain.post.entity.Post;
import com.example.board.domain.post.entity.PostLike;
import com.example.board.domain.post.repository.PostLikeRepository;
import com.example.board.domain.post.repository.PostRepository;
import com.example.board.domain.user.entity.Role;
import com.example.board.domain.user.entity.User;
import com.example.board.domain.user.repository.UserRepository;

/**
 * PostLikeService 단위 테스트
 * 
 * Narrative: 게시글 좋아요 서비스는 사용자가 게시글에 좋아요/싫어요를 토글하고,
 *            자신의 반응 상태를 조회할 수 있는 기능을 제공한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PostLikeService 단위 테스트")
class PostLikeServiceTest {

    @InjectMocks
    private PostLikeService postLikeService;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .loginId("testuser")
                .password("password")
                .username("테스트유저")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testPost = Post.builder()
                .user(testUser)
                .title("테스트 게시글")
                .content("테스트 내용입니다.")
                .build();
        ReflectionTestUtils.setField(testPost, "id", 1L);
    }

    @Nested
    @DisplayName("좋아요 토글 기능")
    class ToggleLikeTest {

        @Test
        @DisplayName("성공: 처음 좋아요를 누르면 좋아요가 생성된다")
        void toggleLike_FirstTime_CreatesLike() {
            // Given: 사용자가 게시글에 처음 좋아요를 누를 때
            Long postId = 1L;
            Long userId = 1L;

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(postLikeRepository.findByPostAndUser(testPost, testUser)).willReturn(Optional.empty());

            // When: 좋아요를 토글하면
            String result = postLikeService.toggleLike(postId, userId);

            // Then: 좋아요가 생성되고 "created"가 반환된다
            assertThat(result).isEqualTo("created");
            assertThat(testPost.getLikeCount()).isEqualTo(1L);
            then(postLikeRepository).should().save(any(PostLike.class));
        }

        @Test
        @DisplayName("성공: 이미 좋아요를 누른 상태에서 다시 누르면 좋아요가 취소된다")
        void toggleLike_AlreadyLiked_CancelsLike() {
            // Given: 사용자가 이미 좋아요를 누른 상태일 때
            Long postId = 1L;
            Long userId = 1L;

            PostLike existingLike = PostLike.builder()
                    .post(testPost)
                    .user(testUser)
                    .likeType(LikeType.LIKE)
                    .build();

            testPost.increaseLikeCount(); // 기존 좋아요로 인해 1

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(postLikeRepository.findByPostAndUser(testPost, testUser)).willReturn(Optional.of(existingLike));

            // When: 좋아요를 다시 토글하면
            String result = postLikeService.toggleLike(postId, userId);

            // Then: 좋아요가 취소되고 "cancelled"가 반환된다
            assertThat(result).isEqualTo("cancelled");
            assertThat(testPost.getLikeCount()).isEqualTo(0L);
            then(postLikeRepository).should().delete(existingLike);
        }

        @Test
        @DisplayName("성공: 싫어요 상태에서 좋아요를 누르면 좋아요로 변경된다")
        void toggleLike_FromDislike_ChangesToLike() {
            // Given: 사용자가 이미 싫어요를 누른 상태일 때
            Long postId = 1L;
            Long userId = 1L;

            PostLike existingDislike = PostLike.builder()
                    .post(testPost)
                    .user(testUser)
                    .likeType(LikeType.DISLIKE)
                    .build();

            testPost.increaseDislikeCount(); // 기존 싫어요로 인해 1

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(postLikeRepository.findByPostAndUser(testPost, testUser)).willReturn(Optional.of(existingDislike));

            // When: 좋아요를 토글하면
            String result = postLikeService.toggleLike(postId, userId);

            // Then: 좋아요로 변경되고 "changed"가 반환된다
            assertThat(result).isEqualTo("changed");
            assertThat(testPost.getLikeCount()).isEqualTo(1L);
            assertThat(testPost.getDislikeCount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 게시글에 좋아요를 누르면 예외가 발생한다")
        void toggleLike_NonExistentPost_ThrowsException() {
            // Given: 존재하지 않는 게시글 ID로 좋아요를 시도할 때
            Long postId = 999L;
            Long userId = 1L;

            given(postRepository.findById(postId)).willReturn(Optional.empty());

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> postLikeService.toggleLike(postId, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 게시글입니다.");
        }
    }

    @Nested
    @DisplayName("싫어요 토글 기능")
    class ToggleDislikeTest {

        @Test
        @DisplayName("성공: 처음 싫어요를 누르면 싫어요가 생성된다")
        void toggleDislike_FirstTime_CreatesDislike() {
            // Given: 사용자가 게시글에 처음 싫어요를 누를 때
            Long postId = 1L;
            Long userId = 1L;

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(postLikeRepository.findByPostAndUser(testPost, testUser)).willReturn(Optional.empty());

            // When: 싫어요를 토글하면
            String result = postLikeService.toggleDisLike(postId, userId);

            // Then: 싫어요가 생성되고 "created"가 반환된다
            assertThat(result).isEqualTo("created");
            assertThat(testPost.getDislikeCount()).isEqualTo(1L);
            then(postLikeRepository).should().save(any(PostLike.class));
        }

        @Test
        @DisplayName("성공: 이미 싫어요를 누른 상태에서 다시 누르면 싫어요가 취소된다")
        void toggleDislike_AlreadyDisliked_CancelsDislike() {
            // Given: 사용자가 이미 싫어요를 누른 상태일 때
            Long postId = 1L;
            Long userId = 1L;

            PostLike existingDislike = PostLike.builder()
                    .post(testPost)
                    .user(testUser)
                    .likeType(LikeType.DISLIKE)
                    .build();

            testPost.increaseDislikeCount(); // 기존 싫어요로 인해 1

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(postLikeRepository.findByPostAndUser(testPost, testUser)).willReturn(Optional.of(existingDislike));

            // When: 싫어요를 다시 토글하면
            String result = postLikeService.toggleDisLike(postId, userId);

            // Then: 싫어요가 취소되고 "cancelled"가 반환된다
            assertThat(result).isEqualTo("cancelled");
            assertThat(testPost.getDislikeCount()).isEqualTo(0L);
            then(postLikeRepository).should().delete(existingDislike);
        }

        @Test
        @DisplayName("성공: 좋아요 상태에서 싫어요를 누르면 싫어요로 변경된다")
        void toggleDislike_FromLike_ChangesToDislike() {
            // Given: 사용자가 이미 좋아요를 누른 상태일 때
            Long postId = 1L;
            Long userId = 1L;

            PostLike existingLike = PostLike.builder()
                    .post(testPost)
                    .user(testUser)
                    .likeType(LikeType.LIKE)
                    .build();

            testPost.increaseLikeCount(); // 기존 좋아요로 인해 1

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(postLikeRepository.findByPostAndUser(testPost, testUser)).willReturn(Optional.of(existingLike));

            // When: 싫어요를 토글하면
            String result = postLikeService.toggleDisLike(postId, userId);

            // Then: 싫어요로 변경되고 "changed"가 반환된다
            assertThat(result).isEqualTo("changed");
            assertThat(testPost.getDislikeCount()).isEqualTo(1L);
            assertThat(testPost.getLikeCount()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("사용자 반응 상태 조회 기능")
    class GetUserLikeTypeTest {

        @Test
        @DisplayName("성공: 좋아요를 누른 사용자의 반응 상태를 조회하면 LIKE가 반환된다")
        void getUserLikeType_WithLike_ReturnsLike() {
            // Given: 사용자가 좋아요를 누른 상태일 때
            Long postId = 1L;
            Long userId = 1L;

            PostLike existingLike = PostLike.builder()
                    .post(testPost)
                    .user(testUser)
                    .likeType(LikeType.LIKE)
                    .build();

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(postLikeRepository.findByPostAndUser(testPost, testUser)).willReturn(Optional.of(existingLike));

            // When: 반응 상태를 조회하면
            LikeType likeType = postLikeService.getUserLikeType(postId, userId);

            // Then: LIKE가 반환된다
            assertThat(likeType).isEqualTo(LikeType.LIKE);
        }

        @Test
        @DisplayName("성공: 싫어요를 누른 사용자의 반응 상태를 조회하면 DISLIKE가 반환된다")
        void getUserLikeType_WithDislike_ReturnsDislike() {
            // Given: 사용자가 싫어요를 누른 상태일 때
            Long postId = 1L;
            Long userId = 1L;

            PostLike existingDislike = PostLike.builder()
                    .post(testPost)
                    .user(testUser)
                    .likeType(LikeType.DISLIKE)
                    .build();

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(postLikeRepository.findByPostAndUser(testPost, testUser)).willReturn(Optional.of(existingDislike));

            // When: 반응 상태를 조회하면
            LikeType likeType = postLikeService.getUserLikeType(postId, userId);

            // Then: DISLIKE가 반환된다
            assertThat(likeType).isEqualTo(LikeType.DISLIKE);
        }

        @Test
        @DisplayName("성공: 반응하지 않은 사용자의 반응 상태를 조회하면 null이 반환된다")
        void getUserLikeType_NoReaction_ReturnsNull() {
            // Given: 사용자가 반응하지 않은 상태일 때
            Long postId = 1L;
            Long userId = 1L;

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(postLikeRepository.findByPostAndUser(testPost, testUser)).willReturn(Optional.empty());

            // When: 반응 상태를 조회하면
            LikeType likeType = postLikeService.getUserLikeType(postId, userId);

            // Then: null이 반환된다
            assertThat(likeType).isNull();
        }

        @Test
        @DisplayName("성공: 비로그인 사용자의 반응 상태를 조회하면 null이 반환된다")
        void getUserLikeType_Anonymous_ReturnsNull() {
            // Given: 비로그인 사용자일 때
            Long postId = 1L;
            Long userId = null;

            // When: 반응 상태를 조회하면
            LikeType likeType = postLikeService.getUserLikeType(postId, userId);

            // Then: null이 반환된다
            assertThat(likeType).isNull();
        }
    }
}

