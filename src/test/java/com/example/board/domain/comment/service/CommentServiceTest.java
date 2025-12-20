package com.example.board.domain.comment.service;

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

import com.example.board.domain.comment.dto.CommentCreateRequest;
import com.example.board.domain.comment.entity.Comment;
import com.example.board.domain.comment.repository.CommentRepository;
import com.example.board.domain.post.entity.Post;
import com.example.board.domain.post.repository.PostRepository;
import com.example.board.domain.user.entity.Role;
import com.example.board.domain.user.entity.User;
import com.example.board.domain.user.repository.UserRepository;

/**
 * CommentService 단위 테스트
 * 
 * Narrative: 댓글 서비스는 게시글에 대한 댓글 작성, 수정, 삭제 기능을 제공하며,
 *            대댓글(답글) 기능도 지원한다.
 *            댓글 수정/삭제는 작성자 본인만 가능하다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 단위 테스트")
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    private User testUser;
    private User otherUser;
    private Post testPost;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .loginId("testuser")
                .password("password")
                .username("테스트유저")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        otherUser = User.builder()
                .loginId("otheruser")
                .password("password")
                .username("다른유저")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(otherUser, "id", 2L);

        testPost = Post.builder()
                .user(testUser)
                .title("테스트 게시글")
                .content("테스트 내용입니다.")
                .build();
        ReflectionTestUtils.setField(testPost, "id", 1L);

        testComment = Comment.builder()
                .content("테스트 댓글입니다.")
                .user(testUser)
                .post(testPost)
                .parent(null)
                .build();
        ReflectionTestUtils.setField(testComment, "id", 1L);
    }

    @Nested
    @DisplayName("댓글 작성 기능")
    class WriteCommentTest {

        @Test
        @DisplayName("성공: 게시글에 댓글을 작성하면 댓글 ID가 반환된다")
        void writeComment_ToPost_ReturnsCommentId() {
            // Given: 유효한 게시글과 사용자가 있을 때
            Long userId = 1L;
            Long postId = 1L;
            CommentCreateRequest request = new CommentCreateRequest();
            request.setContent("새 댓글입니다.");
            request.setParentId(null);

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(commentRepository.save(any(Comment.class))).willReturn(testComment);

            // When: 댓글을 작성하면
            Long commentId = commentService.writeComment(request, userId, postId);

            // Then: 댓글이 저장되고 ID가 반환된다
            assertThat(commentId).isEqualTo(1L);
            then(commentRepository).should().save(any(Comment.class));
        }

        @Test
        @DisplayName("성공: 댓글에 대댓글을 작성하면 대댓글 ID가 반환된다")
        void writeComment_AsReply_ReturnsReplyId() {
            // Given: 부모 댓글이 존재하고 대댓글을 작성할 때
            Long userId = 1L;
            Long postId = 1L;
            Long parentCommentId = 1L;

            CommentCreateRequest request = new CommentCreateRequest();
            request.setContent("대댓글입니다.");
            request.setParentId(parentCommentId);

            Comment replyComment = Comment.builder()
                    .content("대댓글입니다.")
                    .user(testUser)
                    .post(testPost)
                    .parent(testComment)
                    .build();
            ReflectionTestUtils.setField(replyComment, "id", 2L);

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(commentRepository.findById(parentCommentId)).willReturn(Optional.of(testComment));
            given(commentRepository.save(any(Comment.class))).willReturn(replyComment);

            // When: 대댓글을 작성하면
            Long commentId = commentService.writeComment(request, userId, postId);

            // Then: 대댓글이 저장되고 ID가 반환된다
            assertThat(commentId).isEqualTo(2L);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 게시글에 댓글을 작성하면 예외가 발생한다")
        void writeComment_ToNonExistentPost_ThrowsException() {
            // Given: 존재하지 않는 게시글 ID로 댓글 작성을 시도할 때
            Long userId = 1L;
            Long postId = 999L;
            CommentCreateRequest request = new CommentCreateRequest();
            request.setContent("새 댓글입니다.");

            given(postRepository.findById(postId)).willReturn(Optional.empty());

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> commentService.writeComment(request, userId, postId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 게시글입니다.");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 사용자가 댓글을 작성하면 예외가 발생한다")
        void writeComment_ByNonExistentUser_ThrowsException() {
            // Given: 존재하지 않는 사용자 ID로 댓글 작성을 시도할 때
            Long userId = 999L;
            Long postId = 1L;
            CommentCreateRequest request = new CommentCreateRequest();
            request.setContent("새 댓글입니다.");

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> commentService.writeComment(request, userId, postId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 회원입니다.");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 부모 댓글에 대댓글을 작성하면 예외가 발생한다")
        void writeComment_ToNonExistentParent_ThrowsException() {
            // Given: 존재하지 않는 부모 댓글 ID로 대댓글 작성을 시도할 때
            Long userId = 1L;
            Long postId = 1L;
            Long parentCommentId = 999L;

            CommentCreateRequest request = new CommentCreateRequest();
            request.setContent("대댓글입니다.");
            request.setParentId(parentCommentId);

            given(postRepository.findById(postId)).willReturn(Optional.of(testPost));
            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(commentRepository.findById(parentCommentId)).willReturn(Optional.empty());

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> commentService.writeComment(request, userId, postId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 댓글입니다.");
        }
    }

    @Nested
    @DisplayName("댓글 수정 기능")
    class UpdateCommentTest {

        @Test
        @DisplayName("성공: 작성자 본인이 댓글을 수정하면 내용이 변경된다")
        void updateComment_ByOwner_UpdatesContent() {
            // Given: 존재하는 댓글이 있고 작성자 본인이 수정할 때
            Long commentId = 1L;
            Long userId = 1L; // testUser의 ID
            String newContent = "수정된 댓글입니다.";

            given(commentRepository.findById(commentId)).willReturn(Optional.of(testComment));

            // When: 댓글을 수정하면
            commentService.updateComment(commentId, userId, newContent);

            // Then: 댓글 내용이 변경된다
            assertThat(testComment.getContent()).isEqualTo(newContent);
        }

        @Test
        @DisplayName("실패: 작성자가 아닌 사용자가 댓글을 수정하면 예외가 발생한다")
        void updateComment_ByNonOwner_ThrowsException() {
            // Given: 다른 사용자가 댓글을 수정하려고 할 때
            Long commentId = 1L;
            Long otherUserId = 2L; // 다른 유저의 ID
            String newContent = "수정된 댓글입니다.";

            given(commentRepository.findById(commentId)).willReturn(Optional.of(testComment));

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> commentService.updateComment(commentId, otherUserId, newContent))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 댓글을 수정하면 예외가 발생한다")
        void updateComment_NonExistentComment_ThrowsException() {
            // Given: 존재하지 않는 댓글 ID로 수정을 시도할 때
            Long commentId = 999L;
            Long userId = 1L;
            given(commentRepository.findById(commentId)).willReturn(Optional.empty());

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> commentService.updateComment(commentId, userId, "수정 내용"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 댓글입니다.");
        }
    }

    @Nested
    @DisplayName("댓글 삭제 기능")
    class DeleteCommentTest {

        @Test
        @DisplayName("성공: 작성자 본인이 댓글을 삭제하면 소프트 삭제가 적용된다")
        void deleteComment_ByOwner_SoftDeletes() {
            // Given: 존재하는 댓글이 있고 작성자 본인이 삭제할 때
            Long commentId = 1L;
            Long userId = 1L; // testUser의 ID
            given(commentRepository.findById(commentId)).willReturn(Optional.of(testComment));

            // When: 댓글을 삭제하면
            commentService.deleteComment(commentId, userId);

            // Then: 소프트 삭제가 적용된다 (isDeleted = true, content = "삭제된 댓글입니다.")
            assertThat(testComment.isDeleted()).isTrue();
            assertThat(testComment.getContent()).isEqualTo("삭제된 댓글입니다.");
        }

        @Test
        @DisplayName("실패: 작성자가 아닌 사용자가 댓글을 삭제하면 예외가 발생한다")
        void deleteComment_ByNonOwner_ThrowsException() {
            // Given: 다른 사용자가 댓글을 삭제하려고 할 때
            Long commentId = 1L;
            Long otherUserId = 2L; // 다른 유저의 ID

            given(commentRepository.findById(commentId)).willReturn(Optional.of(testComment));

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> commentService.deleteComment(commentId, otherUserId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 댓글을 삭제하면 예외가 발생한다")
        void deleteComment_NonExistentComment_ThrowsException() {
            // Given: 존재하지 않는 댓글 ID로 삭제를 시도할 때
            Long commentId = 999L;
            Long userId = 1L;
            given(commentRepository.findById(commentId)).willReturn(Optional.empty());

            // When & Then: 예외가 발생한다
            assertThatThrownBy(() -> commentService.deleteComment(commentId, userId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("존재하지 않는 댓글입니다.");
        }
    }

    @Nested
    @DisplayName("댓글 수 조회 기능")
    class GetTotalCommentCountTest {

        @Test
        @DisplayName("성공: 게시글의 전체 댓글 수를 조회한다")
        void getTotalCommentCount_ReturnsCount() {
            // Given: 게시글에 댓글이 여러 개 있을 때
            Long postId = 1L;
            Long commentCount = 15L;
            given(commentRepository.countByPostId(postId)).willReturn(commentCount);

            // When: 댓글 수를 조회하면
            Long count = commentService.getTotalCommentCount(postId);

            // Then: 전체 댓글 수가 반환된다
            assertThat(count).isEqualTo(15L);
        }

        @Test
        @DisplayName("성공: 댓글이 없는 게시글의 댓글 수를 조회하면 0이 반환된다")
        void getTotalCommentCount_NoComments_ReturnsZero() {
            // Given: 게시글에 댓글이 없을 때
            Long postId = 1L;
            given(commentRepository.countByPostId(postId)).willReturn(0L);

            // When: 댓글 수를 조회하면
            Long count = commentService.getTotalCommentCount(postId);

            // Then: 0이 반환된다
            assertThat(count).isEqualTo(0L);
        }
    }
}
