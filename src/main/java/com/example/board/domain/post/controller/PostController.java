package com.example.board.domain.post.controller;

import java.util.List;

import org.springframework.stereotype.Controller;

import com.example.board.domain.post.dto.PostResponse;
import com.example.board.domain.post.service.PostService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping
    public String postList(Model model){
        List<PostResponse> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);
        return "post/list";
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public String postDetail(@PathVariable Long postId, Model model, HttpServletRequest request) {
        // 세션에서 로그인 사용자 ID 추출 (비로그인이면 null)
        HttpSession session = request.getSession(false);
        Long loginUserId = null;
        if (session != null && session.getAttribute("loginUserId") != null) {
            loginUserId = (Long) session.getAttribute("loginUserId");
            model.addAttribute("loginUserId", loginUserId);
        }
        
        // Service에 loginUserId 전달 (조회수 처리용)
        PostResponse post = postService.getPost(postId, loginUserId);
        model.addAttribute("post", post);
        
        return "post/detail";
    }

    @GetMapping("/write")
    public String writePostPage(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if(session == null || session.getAttribute("loginUserId") == null){
            return "redirect:/users/login";
        }

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
        Long postId = postService.write(userId, request);
        return "redirect:/posts/" + postId; // 작성한 글 상세페이지로 이동
    }
}