package com.example.board.domain.comment.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.board.domain.comment.dto.CommentCreateRequest;
import com.example.board.domain.comment.dto.CommentUpdateRequest;
import com.example.board.domain.comment.service.CommentService;
import com.example.board.global.security.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 댓글 API 컨트롤러
 * 
 * [Spring Security 적용 후 변경 사항]
 * - @AuthenticationPrincipal로 로그인 사용자 정보 주입
 * - 인증되지 않은 요청은 SecurityConfig에서 자동으로 401 응답
 * - 작성자 본인 여부 검증 로직 추가
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성
     */
    @PostMapping
    public ResponseEntity<?> writeComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long commentId = commentService.writeComment(request, userDetails.getUserId(), postId);
        return ResponseEntity.ok(Map.of(
                "message", "댓글이 작성되었습니다.",
                "commentId", commentId
        ));
    }

    /**
     * 댓글 수정 (작성자 본인만 가능)
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        commentService.updateComment(commentId, userDetails.getUserId(), request.getContent());
        return ResponseEntity.ok(Map.of("message", "댓글이 수정되었습니다."));
    }

    /**
     * 댓글 삭제 (작성자 본인만 가능)
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        commentService.deleteComment(commentId, userDetails.getUserId());
        return ResponseEntity.ok(Map.of("message", "댓글이 삭제되었습니다."));
    }
}
