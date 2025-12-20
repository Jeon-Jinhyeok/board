package com.example.board.domain.post.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.board.domain.bookmark.service.BookmarkService;
import com.example.board.domain.category.dto.CategoryResponse;
import com.example.board.domain.category.service.CategoryService;
import com.example.board.domain.comment.dto.CommentResponse;
import com.example.board.domain.comment.service.CommentService;
import com.example.board.domain.post.dto.PostCreateRequest;
import com.example.board.domain.post.dto.PostResponse;
import com.example.board.domain.post.dto.PostUpdateRequest;
import com.example.board.domain.post.service.PostService;
import com.example.board.global.security.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 게시글 관련 페이지 컨트롤러
 * 
 * [Spring Security 적용 후 변경 사항]
 * - HttpSession 대신 @AuthenticationPrincipal 사용
 * - 인증 필요한 URL은 SecurityConfig에서 자동 체크
 * - 로그인하지 않으면 자동으로 로그인 페이지로 리다이렉트
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final CategoryService categoryService;
    private final CommentService commentService;
    private final BookmarkService bookmarkService;

    /**
     * 게시글 목록 조회 (누구나 접근 가능)
     */
    @GetMapping
    public String postList(
            @RequestParam(required = false) Long categoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
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
        
        if (userDetails != null) {
            model.addAttribute("loginUser", userDetails.getUser());
        }
        
        return "post/list";
    }

    /**
     * 게시글 상세 조회 (누구나 접근 가능)
     */
    @GetMapping("/{postId}")
    public String postDetail(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        // 로그인 사용자 ID (비로그인이면 null)
        Long loginUserId = (userDetails != null) ? userDetails.getUserId() : null;

        // 로그인 사용자 정보
        if (userDetails != null) {
            model.addAttribute("loginUser", userDetails.getUser());
        }

        // 게시글 조회
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

    /**
     * 게시글 작성 페이지 (로그인 필요 - SecurityConfig에서 체크)
     */
    @GetMapping("/write")
    public String writePostPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("postCreateRequest", new PostCreateRequest());
        model.addAttribute("loginUser", userDetails.getUser());

        return "post/write";
    }

    /**
     * 게시글 작성 처리 (로그인 필요)
     */
    @PostMapping("/write")
    public String write(
            @Valid @ModelAttribute PostCreateRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "post/write";
        }

        Long postId = postService.writePost(userDetails.getUserId(), request);
        return "redirect:/posts/" + postId;
    }

    /**
     * 게시글 수정 페이지 (로그인 필요)
     */
    @GetMapping("/{postId}/edit")
    public String editPostPage(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        PostResponse post = postService.getPost(postId, userDetails.getUserId());
        
        // 작성자 본인만 수정 페이지 접근 가능
        if (!post.isOwner()) {
            return "redirect:/posts/" + postId;
        }

        model.addAttribute("post", post);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("loginUser", userDetails.getUser());
        
        return "post/edit";
    }

    /**
     * 게시글 수정 처리 (로그인 필요)
     */
    @PutMapping("/{postId}")
    public String updatePost(
            @PathVariable Long postId,
            @Valid @ModelAttribute PostUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.updatePost(postId, userDetails.getUserId(), request);
        return "redirect:/posts/" + postId;
    }

    /**
     * 게시글 삭제 (로그인 필요)
     */
    @DeleteMapping("/{postId}")
    public String deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.deletePost(postId, userDetails.getUserId());
        return "redirect:/";
    }
}
