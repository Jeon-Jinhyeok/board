package com.example.board.domain.post.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Map;
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

import com.example.board.domain.category.entity.Category;
import com.example.board.domain.category.service.CategoryService;
import com.example.board.domain.post.dto.PostCreateRequest;
import com.example.board.domain.post.dto.PostResponse;
import com.example.board.domain.post.dto.PostUpdateRequest;
import com.example.board.domain.post.entity.Post;
import com.example.board.domain.post.repository.PostRepository;
import com.example.board.domain.user.entity.Role;
import com.example.board.domain.user.entity.User;
import com.example.board.domain.user.repository.UserRepository;

/**
 * PostService 단위 테스트
 * 
 * Narrative: 게시글 서비스는 게시글의 CRUD 기능과 조회수, 좋아요 관련 기능을 제공한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PostService 단위 테스트")
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryService categoryService;

    private User testUser;
    private Post testPost;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .loginId("testuser")
                .password("password")
                .username("테스트유저")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testCategory = Category.builder()
                .name("자유게시판")
                .build();
        ReflectionTestUtils.setField(testCategory, "id", 1L);

        testPost = Post.builder()
                .user(testUser)
                .title("테스트 게시글")
                .content("테스트 내용입니다.")
                .category(testCategory)
                .build();
        ReflectionTestUtils.setField(testPost, "id", 1L);
    }

    @Nested
    @DisplayName("게시글 목록 조회 기능")
    class GetPostsTest {

        @Test
        @DisplayName("성공: 전체 게시글 목록을 최신순으로 조회한다")
        void getAllPosts_ReturnsPostsInDescOrder() {
            // Given: 게시글이 여러 개 존재할 때
            Post post2 = Post.builder()
                    .user(testUser)
                    .title("두 번째 게시글")
                    .content("두 번째 내용")
                    .category(testCategory)
                    .build();
            ReflectionTestUtils.setField(post2, "id", 2L);

            given(postRepository.findAllByOrderByCreatedAtDesc())
                    .willReturn(List.of(post2, testPost));

            // When: 전체 게시글을 조회하면
            List<PostResponse> posts = postService.getAllPosts();

            // Then: 최신순으로 정렬된 게시글 목록이 반환된다
            assertThat(posts).hasSize(2);
            assertThat(posts.get(0).getTitle()).isEqualTo("두 번째 게시글");
            assertThat(posts.get(1).getTitle()).isEqualTo("테스트 게시글");
        }

        @Test
        @DisplayName("성공: 특정 카테고리의 게시글 목록을 조회한다")
        void getPostsByCategory_ReturnsFilteredPosts() {
            // Given: 특정 카테고리의 게시글이 존재할 때
            Long categoryId = 1L;
            given(postRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId))
                    .willReturn(List.of(testPost));

            // When: 카테고리별 게시글을 조회하면
            List<PostResponse> posts = postService.getPostsByCategory(categoryId);

            // Then: 해당 카테고리의 게시글만 반환된다
            assertThat(posts).hasSize(1);
            assertThat(posts.get(0).getTitle()).isEqualTo("테스트 게시글");
        }
    }

    @Nested
    @DisplayName("게시글 상세 조회 기능")
    class GetPostTest {

        @Test
        @DisplayName("성공: 다른 사용자가 게시글을 조회하면 조회수가 증가한다")
        void getPost_ByDifferentUser_IncreasesViewCount() {
            // Given: 게시글이 존재하고 다른 사용자가 조회할 때
            Long postId = 1L;
            Long loginUserId = 2L; // 다른 사용자

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));

            // When: 게시글을 조회하면
            PostResponse response = postService.getPost(postId, loginUserId);

            // Then: 조회수가 증가하고 게시글 정보가 반환된다
            assertThat(response).isNotNull();
            assertThat(testPost.getViewCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("성공: 작성자 본인이 게시글을 조회하면 조회수가 증가하지 않는다")
        void getPost_ByAuthor_DoesNotIncreaseViewCount() {
            // Given: 게시글이 존재하고 작성자 본인이 조회할 때
            Long postId = 1L;
            Long authorId = 1L; // 작성자 본인

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));

            // When: 게시글을 조회하면
            PostResponse response = postService.getPost(postId, authorId);

            // Then: 조회수가 증가하지 않는다
            assertThat(response).isNotNull();
            assertThat(testPost.getViewCount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("성공: 비로그인 사용자가 게시글을 조회하면 조회수가 증가한다")
        void getPost_ByAnonymous_IncreasesViewCount() {
            // Given: 게시글이 존재하고 비로그인 사용자가 조회할 때
            Long postId = 1L;
            Long loginUserId = null;

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));

            // When: 게시글을 조회하면
            PostResponse response = postService.getPost(postId, loginUserId);

            // Then: 조회수가 증가한다
            assertThat(response).isNotNull();
            assertThat(testPost.getViewCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 게시글을 조회하면 예외가 발생한다")
        void getPost_NonExistentPost_ThrowsException() {
            // Given: 존재하지 않는 게시글 ID로 조회할 때
            Long postId = 999L;
            given(postRepository.findById(postId)).willReturn(Optional.empty());

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> postService.getPost(postId, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 게시글입니다.");
        }
    }

    @Nested
    @DisplayName("게시글 작성 기능")
    class WritePostTest {

        @Test
        @DisplayName("성공: 기존 카테고리를 선택하여 게시글을 작성한다")
        void writePost_WithExistingCategory_ReturnsPostId() {
            // Given: 유효한 사용자와 기존 카테고리로 게시글 작성 요청이 있을 때
            Long userId = 1L;
            PostCreateRequest request = new PostCreateRequest();
            request.setTitle("새 게시글");
            request.setContent("새 게시글 내용");
            request.setCategoryId(1L);

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(categoryService.findById(1L)).willReturn(testCategory);
            given(postRepository.save(any(Post.class))).willReturn(testPost);

            // When: 게시글을 작성하면
            Long postId = postService.writePost(userId, request);

            // Then: 게시글이 저장되고 ID가 반환된다
            assertThat(postId).isEqualTo(1L);
            then(postRepository).should().save(any(Post.class));
        }

        @Test
        @DisplayName("성공: 새 카테고리를 생성하여 게시글을 작성한다")
        void writePost_WithNewCategory_CreatesCategory() {
            // Given: 새 카테고리 이름으로 게시글 작성 요청이 있을 때
            Long userId = 1L;
            PostCreateRequest request = new PostCreateRequest();
            request.setTitle("새 게시글");
            request.setContent("새 게시글 내용");
            request.setNewCategoryName("새카테고리");

            Category newCategory = Category.builder().name("새카테고리").build();

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(categoryService.getOrCreateCategory("새카테고리")).willReturn(newCategory);
            given(postRepository.save(any(Post.class))).willReturn(testPost);

            // When: 게시글을 작성하면
            Long postId = postService.writePost(userId, request);

            // Then: 새 카테고리가 생성되고 게시글이 저장된다
            assertThat(postId).isEqualTo(1L);
            then(categoryService).should().getOrCreateCategory("새카테고리");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자가 게시글을 작성하면 예외가 발생한다")
        void writePost_NonExistentUser_ThrowsException() {
            // Given: 존재하지 않는 사용자 ID로 게시글 작성을 시도할 때
            Long userId = 999L;
            PostCreateRequest request = new PostCreateRequest();
            request.setTitle("새 게시글");
            request.setContent("새 게시글 내용");

            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> postService.writePost(userId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 회원입니다.");
        }
    }

    @Nested
    @DisplayName("게시글 수정 기능")
    class UpdatePostTest {

        @Test
        @DisplayName("성공: 작성자가 본인의 게시글을 수정한다")
        void updatePost_ByAuthor_UpdatesSuccessfully() {
            // Given: 게시글 작성자가 수정을 요청할 때
            Long postId = 1L;
            Long authorId = 1L;
            PostUpdateRequest request = new PostUpdateRequest();
            request.setTitle("수정된 제목");
            request.setContent("수정된 내용");
            request.setCategoryId(1L);

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
            given(categoryService.findById(1L)).willReturn(testCategory);

            // When: 게시글을 수정하면
            postService.updatePost(postId, authorId, request);

            // Then: 게시글이 수정된다
            assertThat(testPost.getTitle()).isEqualTo("수정된 제목");
            assertThat(testPost.getContent()).isEqualTo("수정된 내용");
        }

        @Test
        @DisplayName("실패: 다른 사용자가 게시글을 수정하려 하면 예외가 발생한다")
        void updatePost_ByNonAuthor_ThrowsException() {
            // Given: 게시글 작성자가 아닌 사용자가 수정을 요청할 때
            Long postId = 1L;
            Long nonAuthorId = 2L;
            PostUpdateRequest request = new PostUpdateRequest();
            request.setTitle("수정된 제목");
            request.setContent("수정된 내용");

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> postService.updatePost(postId, nonAuthorId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 게시글을 수정하려 하면 예외가 발생한다")
        void updatePost_NonExistentPost_ThrowsException() {
            // Given: 존재하지 않는 게시글 ID로 수정을 시도할 때
            Long postId = 999L;
            PostUpdateRequest request = new PostUpdateRequest();

            given(postRepository.findById(postId)).willReturn(Optional.empty());

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> postService.updatePost(postId, 1L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 게시글입니다.");
        }
    }

    @Nested
    @DisplayName("게시글 삭제 기능")
    class DeletePostTest {

        @Test
        @DisplayName("성공: 작성자가 본인의 게시글을 삭제한다")
        void deletePost_ByAuthor_DeletesSuccessfully() {
            // Given: 게시글 작성자가 삭제를 요청할 때
            Long postId = 1L;
            Long authorId = 1L;

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));

            // When: 게시글을 삭제하면
            postService.deletePost(postId, authorId);

            // Then: 게시글이 삭제된다
            then(postRepository).should().delete(testPost);
        }

        @Test
        @DisplayName("실패: 다른 사용자가 게시글을 삭제하려 하면 예외가 발생한다")
        void deletePost_ByNonAuthor_ThrowsException() {
            // Given: 게시글 작성자가 아닌 사용자가 삭제를 요청할 때
            Long postId = 1L;
            Long nonAuthorId = 2L;

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> postService.deletePost(postId, nonAuthorId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }
    }

    @Nested
    @DisplayName("게시글 좋아요 수 조회 기능")
    class GetPostLikeCountsTest {

        @Test
        @DisplayName("성공: 게시글의 좋아요와 싫어요 수를 조회한다")
        void getPostLikeCounts_ReturnsLikeCounts() {
            // Given: 게시글이 존재하고 좋아요/싫어요가 있을 때
            Long postId = 1L;
            ReflectionTestUtils.setField(testPost, "likeCount", 10L);
            ReflectionTestUtils.setField(testPost, "dislikeCount", 2L);

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));

            // When: 좋아요 수를 조회하면
            Map<String, Long> likeCounts = postService.getPostLikeCounts(postId);

            // Then: 좋아요와 싫어요 수가 반환된다
            assertThat(likeCounts.get("likeCount")).isEqualTo(10L);
            assertThat(likeCounts.get("dislikeCount")).isEqualTo(2L);
        }
    }
}

