package com.example.board.domain.comment.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.board.domain.comment.dto.CommentCreateRequest;
import com.example.board.domain.comment.dto.CommentResponse;
import com.example.board.domain.comment.entity.Comment;
import com.example.board.domain.comment.repository.CommentRepository;
import com.example.board.domain.post.entity.Post;
import com.example.board.domain.post.repository.PostRepository;
import com.example.board.domain.user.entity.User;
import com.example.board.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 작성
    @Transactional
    public Long writeComment(CommentCreateRequest request, Long userId, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(
            () -> new IllegalArgumentException("존재하지 않는 게시글입니다.")
        );

        User user = userRepository.findById(userId).orElseThrow(
            () -> new IllegalArgumentException("존재하지 않는 회원입니다.")
        );

        Comment parent = null;
        Long parentId = request.getParentId();
        if(parentId != null){
            parent = commentRepository.findById(parentId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 댓글입니다.")
            );
        }

        Comment comment = Comment.builder()
            .content(request.getContent())
            .post(post)
            .user(user)
            .parent(parent)
            .build();
        
        return commentRepository.save(comment).getId();
    }
    // 댓글 수정
    @Transactional
    public void updateComment(Long commentId, String content){
        Comment comment = commentRepository.findById(commentId).orElseThrow(
            () -> new IllegalArgumentException("존재하지 않는 댓글입니다.")
        );

        comment.update(content);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId){
        Comment comment = commentRepository.findById(commentId).orElseThrow(
            () -> new IllegalArgumentException("존재하지 않는 댓글입니다.")
        );

        comment.delete(); // soft delete
    }

    // 게시글별 댓글 목록 조회 (대댓글 포함, 페이징)
    public Page<CommentResponse> getCommentsByPostId(Long postId, Long loginUserId, Pageable pageable) {
        // 최상위 댓글 페이징 조회 (ToOne 관계인 User만 fetch join)
        Page<Comment> parentComments = commentRepository.findParentCommentsByPostIdWithUser(postId, pageable);

        // 조회된 부모 댓글들의 ID 리스트 추출
        List<Long> parentIds = parentComments.getContent().stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        // 대댓글 조회 (IN 쿼리로 한 번에)
        Map<Long, List<Comment>> repliesMap = Map.of();
        if (!parentIds.isEmpty()) {
            List<Comment> replies = commentRepository.findRepliesByParentIdsWithUser(parentIds);
            repliesMap = replies.stream()
                    .collect(Collectors.groupingBy(c -> c.getParent().getId()));
        }

        // CommentResponse로 변환
        final Map<Long, List<Comment>> finalRepliesMap = repliesMap;
        return parentComments.map(c -> 
            CommentResponse.of(c, finalRepliesMap.getOrDefault(c.getId(), List.of()), loginUserId)
        );
    }

    // 게시글의 전체 댓글 수 조회
    public Long getTotalCommentCount(Long postId) {
        return commentRepository.countByPostId(postId);
    }
}
