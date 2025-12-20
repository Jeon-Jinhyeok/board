package com.example.board.domain.bookmark.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.board.domain.bookmark.service.BookmarkService;
import com.example.board.domain.post.dto.PostResponse;
import com.example.board.global.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

/**
 * 북마크 페이지 컨트롤러
 * 
 * [Spring Security 적용 후 변경 사항]
 * - @AuthenticationPrincipal로 로그인 사용자 정보 주입
 * - 인증되지 않은 사용자는 SecurityConfig에서 자동으로 로그인 페이지로 리다이렉트
 */
@Controller
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkPageController {

    private final BookmarkService bookmarkService;

    /**
     * 북마크 목록 페이지
     * 로그인 필요 - SecurityConfig에서 자동 체크
     */
    @GetMapping
    public String bookmarkList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        // 로그인 사용자 정보 추가
        model.addAttribute("loginUser", userDetails.getUser());

        // 북마크한 게시글 목록 조회
        List<PostResponse> bookmarkedPosts = bookmarkService.getBookmarkedPosts(userDetails.getUserId());
        model.addAttribute("posts", bookmarkedPosts);

        return "bookmark/list";
    }
}
