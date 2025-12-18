package com.example.board.domain.post.dto;

import java.time.LocalDateTime;

import com.example.board.domain.post.entity.Post;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String writer;
    private Long viewCount;
    private Long likeCount;
    private Long dislikeCount;
    private Long bookmarkCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> DTO (목록용 - 간단한 정보만)
    public static PostResponse fromList(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .writer(post.getUser().getUsername())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .build();
    }

    // Entity -> DTO (상세용 - 모든 정보)
    public static PostResponse fromDetail(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writer(post.getUser().getUsername())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .dislikeCount(post.getDislikeCount())
                .bookmarkCount(post.getBookmarkCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
