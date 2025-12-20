package com.example.board.domain.post.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.board.domain.post.service.PostLikeService;
import com.example.board.domain.post.service.PostService;
import com.example.board.global.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

/**
 * 게시글 좋아요/싫어요 API 컨트롤러
 * 
 * [Spring Security 적용 후 변경 사항]
 * - @AuthenticationPrincipal로 로그인 사용자 정보 주입
 * - 인증 필요한 API는 SecurityConfig에서 자동 체크
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostLikeController {

    private final PostLikeService postLikeService;
    private final PostService postService;

    /**
     * 좋아요 토글
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String result = postLikeService.toggleLike(postId, userDetails.getUserId());
        return createResponse(postId, result);
    }

    /**
     * 싫어요 토글
     */
    @PostMapping("/{postId}/dislike")
    public ResponseEntity<?> toggleDislike(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String result = postLikeService.toggleDisLike(postId, userDetails.getUserId());
        return createResponse(postId, result);
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
