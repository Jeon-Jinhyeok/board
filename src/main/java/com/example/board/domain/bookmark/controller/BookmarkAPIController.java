package com.example.board.domain.bookmark.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.board.domain.bookmark.service.BookmarkService;
import com.example.board.global.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

/**
 * 북마크 API 컨트롤러
 * 
 * [Spring Security 적용 후 변경 사항]
 * - @AuthenticationPrincipal로 로그인 사용자 정보 주입
 * - 인증 필요한 API는 SecurityConfig에서 자동 체크
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class BookmarkAPIController {

    private final BookmarkService bookmarkService;

    /**
     * 북마크 토글 (추가/삭제)
     */
    @PostMapping("/{postId}")
    public ResponseEntity<?> toggleBookmark(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean isBookmarked = bookmarkService.toggleBookmark(userDetails.getUserId(), postId);
        String message = isBookmarked ? "북마크에 추가되었습니다." : "북마크가 해제되었습니다.";

        return ResponseEntity.ok(Map.of(
                "bookmarked", isBookmarked,
                "message", message
        ));
    }
}
