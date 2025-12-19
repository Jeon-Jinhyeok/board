package com.example.board.domain.post.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;

import com.example.board.domain.bookmark.service.BookmarkService;
import com.example.board.domain.category.dto.CategoryResponse;
import com.example.board.domain.category.service.CategoryService;
import com.example.board.domain.comment.dto.CommentResponse;
import com.example.board.domain.comment.service.CommentService;
import com.example.board.domain.post.dto.PostResponse;
import com.example.board.domain.post.dto.PostUpdateRequest;
import com.example.board.domain.post.service.PostService;
import com.example.board.domain.user.entity.User;
import com.example.board.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.example.board.domain.post.dto.PostCreateRequest;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;
    private final CategoryService categoryService;
    private final CommentService commentService;
    private final BookmarkService bookmarkService;
    private final UserService userService;

    @GetMapping
    public String postList(
            @RequestParam(required = false) Long categoryId,
            Model model
    ){
        List<PostResponse> posts;
        if (categoryId != null) {
            posts = postService.getPostsByCategory(categoryId);
            model.addAttribute("selectedCategoryId", categoryId);
        } else {
            posts = postService.getAllPosts();
        }
        
        List<CategoryResponse> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("posts", posts);
        return "post/list";
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public String postDetail(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpServletRequest request
    ) {
        // 세션에서 로그인 사용자 ID 추출 (비로그인이면 null)
        HttpSession session = request.getSession(false);
        Long loginUserId = null;
        if (session != null && session.getAttribute("loginUserId") != null) {
            loginUserId = (Long) session.getAttribute("loginUserId");
        }
        
        User loginUser = userService.findById(loginUserId);
        if (loginUser != null) {
            model.addAttribute("loginUser", loginUser);
        }
        model.addAttribute("loginUser", loginUser);
        PostResponse post = postService.getPost(postId, loginUserId);
        model.addAttribute("post", post);

        // 북마크 여부 조회
        boolean isBookmarked = bookmarkService.isBookmarked(loginUserId, postId);
        model.addAttribute("isBookmarked", isBookmarked);

        // 댓글 목록 조회 
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponse> comments = commentService.getCommentsByPostId(postId, loginUserId, pageable);
        model.addAttribute("comments", comments);
        model.addAttribute("totalCommentCount", commentService.getTotalCommentCount(postId));
        
        return "post/detail";
    }

    @GetMapping("/write")
    public String writePostPage(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if(session == null || session.getAttribute("loginUserId") == null){
            return "redirect:/users/login";
        }

        List<CategoryResponse> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("postCreateRequest", new PostCreateRequest());

        return "post/write";
    }

    @PostMapping("/write")
    public String write(@Valid @ModelAttribute PostCreateRequest request,
        BindingResult bindingResult,
        HttpServletRequest httpRequest
    )
    {
        HttpSession session = httpRequest.getSession(false);
        if(session == null || session.getAttribute("loginUserId") == null){
            return "redirect:/users/login";
        }

        // 유효성 검사(제목, 내용 빈칸 유무)
        if(bindingResult.hasErrors()){
            return "post/write";
        }

        Long userId = (Long) session.getAttribute("loginUserId");
        // 저장
        Long postId = postService.writePost(userId, request);
        return "redirect:/posts/" + postId; // 작성한 글 상세페이지로 이동
    }

    @GetMapping("/{postId}/edit")
    public String editPostPage(@PathVariable Long postId, Model model, HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if(session == null || session.getAttribute("loginUserId") == null){
            return "redirect:/users/login";
        }

        Long userId = (Long) session.getAttribute("loginUserId");
        PostResponse post = postService.getPost(postId, userId);

        model.addAttribute("post", post);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "post/edit";
    }

    @PutMapping("/{postId}")
    public String updatePost(
        @PathVariable Long postId,
        @Valid @ModelAttribute PostUpdateRequest request,
        HttpServletRequest httpRequest
    ){
        HttpSession session = httpRequest.getSession(false);
        if(session == null || session.getAttribute("loginUserId") == null){
            return "redirect:/users/login";
        }

        Long userId = (Long) session.getAttribute("loginUserId");
        postService.updatePost(postId, userId, request);
        return "redirect:/posts/" + postId;
    }

    // 게시물 삭제
    @DeleteMapping("/{postId}")
    public String deletePost(@PathVariable Long postId, HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if(session == null || session.getAttribute("loginUserId") == null){
            return "redirect:/users/login";
        }

        Long userId = (Long) session.getAttribute("loginUserId");
        postService.deletePost(postId, userId);
        return "redirect:/";
    }
    
}