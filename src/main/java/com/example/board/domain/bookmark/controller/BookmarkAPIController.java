package com.example.board.domain.bookmark.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.board.domain.bookmark.service.BookmarkService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class BookmarkAPIController {
    private final BookmarkService bookmarkService;

    // 북마크 토글 (추가/삭제)
    @PostMapping("/{postId}")
    public ResponseEntity<?> toggleBookmark(
            @PathVariable Long postId,
            HttpServletRequest request
    ) {
        Long userId = getLoginUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }

        boolean isBookmarked = bookmarkService.toggleBookmark(userId, postId);
        String message = isBookmarked ? "북마크에 추가되었습니다." : "북마크가 해제되었습니다.";
        
        return ResponseEntity.ok(Map.of(
                "bookmarked", isBookmarked,
                "message", message
        ));
    }

    private Long getLoginUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (Long) session.getAttribute("loginUserId") : null;
    }
}
