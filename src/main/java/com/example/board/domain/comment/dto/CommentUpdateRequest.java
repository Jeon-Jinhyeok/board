package com.example.board.domain.comment.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentUpdateRequest {
    @NotBlank(message="댓글을 입력하세요.")
    @Size(max=500, message="댓글은 최대 500자까지 작성할 수 있습니다.")
    private String content;
}