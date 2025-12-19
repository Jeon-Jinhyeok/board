package com.example.board.domain.post.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.board.domain.post.service.PostLikeService;
import com.example.board.domain.post.service.PostService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostLikeController {

    private final PostLikeService postLikeService;
    private final PostService postService;

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long postId, HttpServletRequest request){
        
        Long userId = getLoginUserId(request);
        if(userId == null){
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }

        String result = postLikeService.toggleLike(postId, userId);
        return createResponse(postId, result);
    }

    @PostMapping("/{postId}/dislike")
    public ResponseEntity<?> toggleDislike(@PathVariable Long postId, HttpServletRequest request){
        Long userId = getLoginUserId(request);
        if(userId == null){
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }

        String result = postLikeService.toggleDisLike(postId, userId);
        return createResponse(postId, result);
    }

    private Long getLoginUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (Long) session.getAttribute("loginUserId") : null;
    }

    private ResponseEntity<?> createResponse(Long postId, String result) {
        Map<String, Long> counts = postService.getPostLikeCounts(postId);
        return ResponseEntity.ok(Map.of(
            "result", result,
            "likeCount", counts.get("likeCount"),
            "dislikeCount", counts.get("dislikeCount")
        ));
    }
}
