package com.example.board.domain.home.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.board.domain.category.dto.CategoryResponse;
import com.example.board.domain.category.service.CategoryService;
import com.example.board.domain.post.dto.PostResponse;
import com.example.board.domain.post.service.PostService;
import com.example.board.global.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

/**
 * 홈 페이지 컨트롤러
 * 
 * [@AuthenticationPrincipal 사용법]
 * - Spring Security가 인증된 사용자 정보를 자동으로 주입합니다.
 * - 로그인하지 않은 경우 null이 주입됩니다.
 * - CustomUserDetails 타입으로 받아 사용자 정보에 접근합니다.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PostService postService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String home(
            @RequestParam(required = false) Long categoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        // 게시글 목록 조회 (카테고리 필터 적용)
        List<PostResponse> posts;
        if (categoryId != null) {
            posts = postService.getPostsByCategory(categoryId);
            model.addAttribute("selectedCategoryId", categoryId);
        } else {
            posts = postService.getAllPosts();
        }
        model.addAttribute("posts", posts);

        // 카테고리 목록 조회
        List<CategoryResponse> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        // 로그인 사용자 정보 추가 (비로그인 시 userDetails는 null)
        if (userDetails != null) {
            model.addAttribute("loginUser", userDetails.getUser());
        }

        return "index";
    }
}
