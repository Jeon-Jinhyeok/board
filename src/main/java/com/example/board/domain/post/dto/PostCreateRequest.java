package com.example.board.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCreateRequest {
    @NotBlank(message="제목을 입력해주세요.")
    private String title;

    @NotBlank(message="내용을 입력해주세요.")
    private String content;

    private Long categoryId; // 기존 카테고리 선택 시
    private String newCategoryName; // 새 카테고리 생성 시
}
