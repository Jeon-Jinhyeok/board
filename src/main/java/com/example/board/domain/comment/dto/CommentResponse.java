package com.example.board.domain.comment.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.board.domain.comment.entity.Comment;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentResponse {
    private final Long id;
    private final String content;
    private final String writerName;
    private final Long writerId;
    private final LocalDateTime createdAt;
    private final boolean isDeleted;
    private final boolean isOwner;
    private final List<CommentResponse> replies;

    @Builder
    private CommentResponse(Long id, String content, String writerName, Long writerId,
                           LocalDateTime createdAt, boolean isDeleted, boolean isOwner,
                           List<CommentResponse> replies) {
        this.id = id;
        this.content = content;
        this.writerName = writerName;
        this.writerId = writerId;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
        this.isOwner = isOwner;
        this.replies = replies != null ? replies : new ArrayList<>();
    }

    // 단일 댓글 변환 (대댓글 없이)
    public static CommentResponse from(Comment comment, Long loginUserId) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .writerName(comment.getUser().getUsername())
                .writerId(comment.getUser().getId())
                .createdAt(comment.getCreatedAt())
                .isDeleted(comment.isDeleted())
                .isOwner(loginUserId != null && loginUserId.equals(comment.getUser().getId()))
                .replies(new ArrayList<>())
                .build();
    }

    // 부모 댓글 변환 (대댓글 포함)
    public static CommentResponse of(Comment comment, List<Comment> replyComments, Long loginUserId) {
        List<CommentResponse> replies = replyComments.stream()
                .map(reply -> CommentResponse.from(reply, loginUserId))
                .toList();

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .writerName(comment.getUser().getUsername())
                .writerId(comment.getUser().getId())
                .createdAt(comment.getCreatedAt())
                .isDeleted(comment.isDeleted())
                .isOwner(loginUserId != null && loginUserId.equals(comment.getUser().getId()))
                .replies(replies)
                .build();
    }
}
