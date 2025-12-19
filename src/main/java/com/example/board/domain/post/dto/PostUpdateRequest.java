package com.example.board.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostUpdateRequest {
    @NotBlank(message="제목을 입력해주세요.")
    private String title;

    @NotBlank(message="내용을 입력해주세요.")
    private String content;

    private Long categoryId;
}
