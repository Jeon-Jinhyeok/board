package com.example.board.domain.comment.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {
    private final CommentService commentService;

    // 댓글 작성
    @PostMapping
    public ResponseEntity<?> writeComment(
        @PathVariable Long postId,
        @Valid @RequestBody CommentCreateRequest request,
        HttpServletRequest httpRequest
    ){
        Long userId = getLoginUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }

        commentService.writeComment(request, userId, postId);
        return ResponseEntity.ok(Map.of("message", "댓글이 작성되었습니다."));
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
        @PathVariable Long postId,
        @PathVariable Long commentId,
        HttpServletRequest httpRequest,
        @Valid @RequestBody CommentUpdateRequest request
    ){
        Long userId = getLoginUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }

        commentService.updateComment(commentId, request.getContent());
        return ResponseEntity.ok(Map.of("message", "댓글이 수정되었습니다."));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
        @PathVariable Long postId,
        @PathVariable Long commentId,
        HttpServletRequest httpRequest
    ){
        Long userId = getLoginUserId(httpRequest);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }

        commentService.deleteComment(commentId);
        return ResponseEntity.ok(Map.of("message", "댓글이 삭제되었습니다."));
    }

    private Long getLoginUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (Long) session.getAttribute("loginUserId") : null;
    }
}
