package com.example.board.domain.bookmark.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.board.domain.bookmark.service.BookmarkService;
import com.example.board.domain.post.dto.PostResponse;
import com.example.board.domain.user.entity.User;
import com.example.board.domain.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkPageController {

    private final BookmarkService bookmarkService;
    private final UserService userService;

    // 북마크 목록 페이지
    @GetMapping
    public String bookmarkList(Model model, HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        
        // 비로그인 시 로그인 페이지로 리다이렉트
        if (userId == null) {
            return "redirect:/users/login";
        }

        // 로그인 사용자 정보 추가 (네비게이션 바에 이름 표시용)
        User loginUser = userService.findById(userId);
        if (loginUser != null) {
            model.addAttribute("loginUser", loginUser);
        }

        List<PostResponse> bookmarkedPosts = bookmarkService.getBookmarkedPosts(userId);
        model.addAttribute("posts", bookmarkedPosts);
        return "bookmark/list";
    }

    private Long getLoginUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (session != null) ? (Long) session.getAttribute("loginUserId") : null;
    }
}
