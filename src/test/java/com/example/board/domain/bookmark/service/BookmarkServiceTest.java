package com.example.board.domain.bookmark.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
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

import com.example.board.domain.bookmark.entity.Bookmark;
import com.example.board.domain.bookmark.repository.BookmarkRepository;
import com.example.board.domain.post.dto.PostResponse;
import com.example.board.domain.post.entity.Post;
import com.example.board.domain.post.repository.PostRepository;
import com.example.board.domain.user.entity.Role;
import com.example.board.domain.user.entity.User;
import com.example.board.domain.user.repository.UserRepository;

/**
 * BookmarkService 단위 테스트
 * 
 * Narrative: 북마크 서비스는 사용자가 게시글을 북마크하고 관리할 수 있는 기능을 제공한다.
 *            북마크 토글, 추가, 삭제, 조회 기능을 지원한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookmarkService 단위 테스트")
class BookmarkServiceTest {

    @InjectMocks
    private BookmarkService bookmarkService;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    private User testUser;
    private Post testPost;
    private Bookmark testBookmark;

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

        testBookmark = Bookmark.builder()
                .user(testUser)
                .post(testPost)
                .build();
        ReflectionTestUtils.setField(testBookmark, "id", 1L);
    }

    @Nested
    @DisplayName("북마크 토글 기능")
    class ToggleBookmarkTest {

        @Test
        @DisplayName("성공: 북마크가 없는 상태에서 토글하면 북마크가 추가되고 true를 반환한다")
        void toggleBookmark_WhenNotBookmarked_AddsBookmark() {
            // Given: 사용자가 게시글을 북마크하지 않은 상태일 때
            Long userId = 1L;
            Long postId = 1L;

            given(bookmarkRepository.existsByUserIdAndPostId(userId, postId)).willReturn(false);
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));

            // When: 북마크를 토글하면
            boolean result = bookmarkService.toggleBookmark(userId, postId);

            // Then: 북마크가 추가되고 true가 반환된다
            assertThat(result).isTrue();
            then(bookmarkRepository).should().save(any(Bookmark.class));
        }

        @Test
        @DisplayName("성공: 북마크가 있는 상태에서 토글하면 북마크가 삭제되고 false를 반환한다")
        void toggleBookmark_WhenAlreadyBookmarked_RemovesBookmark() {
            // Given: 사용자가 이미 게시글을 북마크한 상태일 때
            Long userId = 1L;
            Long postId = 1L;

            given(bookmarkRepository.existsByUserIdAndPostId(userId, postId)).willReturn(true);

            // When: 북마크를 토글하면
            boolean result = bookmarkService.toggleBookmark(userId, postId);

            // Then: 북마크가 삭제되고 false가 반환된다
            assertThat(result).isFalse();
            then(bookmarkRepository).should().deleteByUserIdAndPostId(userId, postId);
        }
    }

    @Nested
    @DisplayName("북마크 추가 기능")
    class AddBookmarkTest {

        @Test
        @DisplayName("성공: 유효한 사용자와 게시글로 북마크를 추가한다")
        void addBookmark_WithValidUserAndPost_AddsBookmark() {
            // Given: 유효한 사용자와 게시글이 있을 때
            Long userId = 1L;
            Long postId = 1L;

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));

            // When: 북마크를 추가하면
            bookmarkService.addBookmark(userId, postId);

            // Then: 북마크가 저장된다
            then(bookmarkRepository).should().save(any(Bookmark.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자로 북마크를 추가하면 예외가 발생한다")
        void addBookmark_WithNonExistentUser_ThrowsException() {
            // Given: 존재하지 않는 사용자 ID로 북마크 추가를 시도할 때
            Long userId = 999L;
            Long postId = 1L;

            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> bookmarkService.addBookmark(userId, postId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("사용자를 찾을 수 없습니다.");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 게시글로 북마크를 추가하면 예외가 발생한다")
        void addBookmark_WithNonExistentPost_ThrowsException() {
            // Given: 존재하지 않는 게시글 ID로 북마크 추가를 시도할 때
            Long userId = 1L;
            Long postId = 999L;

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(postRepository.findById(postId)).willReturn(Optional.empty());

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> bookmarkService.addBookmark(userId, postId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("북마크 삭제 기능")
    class RemoveBookmarkTest {

        @Test
        @DisplayName("성공: 북마크를 삭제한다")
        void removeBookmark_RemovesSuccessfully() {
            // Given: 북마크가 존재할 때
            Long userId = 1L;
            Long postId = 1L;

            // When: 북마크를 삭제하면
            bookmarkService.removeBookmark(userId, postId);

            // Then: 북마크가 삭제된다
            then(bookmarkRepository).should().deleteByUserIdAndPostId(userId, postId);
        }
    }

    @Nested
    @DisplayName("북마크한 게시글 목록 조회 기능")
    class GetBookmarkedPostsTest {

        @Test
        @DisplayName("성공: 사용자가 북마크한 게시글 목록을 조회한다")
        void getBookmarkedPosts_ReturnsBookmarkedPosts() {
            // Given: 사용자가 북마크한 게시글들이 있을 때
            Long userId = 1L;

            Post post2 = Post.builder()
                    .user(testUser)
                    .title("두 번째 게시글")
                    .content("두 번째 내용")
                    .build();
            ReflectionTestUtils.setField(post2, "id", 2L);

            Bookmark bookmark2 = Bookmark.builder()
                    .user(testUser)
                    .post(post2)
                    .build();

            given(bookmarkRepository.findByUserId(userId))
                    .willReturn(List.of(testBookmark, bookmark2));

            // When: 북마크한 게시글 목록을 조회하면
            List<PostResponse> posts = bookmarkService.getBookmarkedPosts(userId);

            // Then: 북마크한 게시글 목록이 반환된다
            assertThat(posts).hasSize(2);
        }

        @Test
        @DisplayName("성공: 북마크한 게시글이 없으면 빈 목록을 반환한다")
        void getBookmarkedPosts_WhenEmpty_ReturnsEmptyList() {
            // Given: 사용자가 북마크한 게시글이 없을 때
            Long userId = 1L;
            given(bookmarkRepository.findByUserId(userId)).willReturn(List.of());

            // When: 북마크한 게시글 목록을 조회하면
            List<PostResponse> posts = bookmarkService.getBookmarkedPosts(userId);

            // Then: 빈 목록이 반환된다
            assertThat(posts).isEmpty();
        }
    }

    @Nested
    @DisplayName("북마크 여부 확인 기능")
    class IsBookmarkedTest {

        @Test
        @DisplayName("성공: 북마크한 게시글이면 true를 반환한다")
        void isBookmarked_WhenBookmarked_ReturnsTrue() {
            // Given: 사용자가 게시글을 북마크한 상태일 때
            Long userId = 1L;
            Long postId = 1L;

            given(bookmarkRepository.existsByUserIdAndPostId(userId, postId)).willReturn(true);

            // When: 북마크 여부를 확인하면
            boolean result = bookmarkService.isBookmarked(userId, postId);

            // Then: true가 반환된다
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공: 북마크하지 않은 게시글이면 false를 반환한다")
        void isBookmarked_WhenNotBookmarked_ReturnsFalse() {
            // Given: 사용자가 게시글을 북마크하지 않은 상태일 때
            Long userId = 1L;
            Long postId = 1L;

            given(bookmarkRepository.existsByUserIdAndPostId(userId, postId)).willReturn(false);

            // When: 북마크 여부를 확인하면
            boolean result = bookmarkService.isBookmarked(userId, postId);

            // Then: false가 반환된다
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("성공: 비로그인 사용자는 항상 false를 반환한다")
        void isBookmarked_WhenAnonymous_ReturnsFalse() {
            // Given: 비로그인 사용자일 때
            Long userId = null;
            Long postId = 1L;

            // When: 북마크 여부를 확인하면
            boolean result = bookmarkService.isBookmarked(userId, postId);

            // Then: false가 반환된다
            assertThat(result).isFalse();
        }
    }
}

