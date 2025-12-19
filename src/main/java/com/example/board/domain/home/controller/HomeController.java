package com.example.board.domain.home.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.board.domain.category.dto.CategoryResponse;
import com.example.board.domain.category.service.CategoryService;
import com.example.board.domain.post.dto.PostResponse;
import com.example.board.domain.post.service.PostService;
import com.example.board.domain.user.entity.User;
import com.example.board.domain.user.service.UserService;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final UserService userService;
    private final PostService postService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String home(
            @RequestParam(required = false) Long categoryId,
            HttpServletRequest request,
            Model model
    ){
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
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginUserId") == null){
            return "index";
        }

        Long userId = (Long) session.getAttribute("loginUserId");
        User loginUser = userService.findById(userId);

        if(loginUser != null){
            model.addAttribute("loginUser", loginUser);
        }

        return "index";
    }
}
