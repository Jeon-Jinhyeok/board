package com.example.board.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.board.domain.user.dto.LoginRequest;
import com.example.board.domain.user.dto.SignupRequest;
import com.example.board.domain.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 관련 페이지 컨트롤러
 * 
 * [Spring Security 적용 후 변경 사항]
 * - 로그인 처리: Spring Security의 formLogin이 자동 처리
 * - 로그아웃 처리: Spring Security의 logout이 자동 처리
 * - 세션 관리: Spring Security가 SecurityContext를 통해 자동 관리
 * 
 * 따라서 이 컨트롤러에서는:
 * - 회원가입 페이지/처리만 담당
 * - 로그인/로그아웃 페이지만 제공 (실제 처리는 Security가 담당)
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 페이지
     */
    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "user/signup";
    }

    /**
     * 회원가입 처리
     * 회원가입은 Spring Security와 무관하게 직접 처리합니다.
     */
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupRequest request, 
                         BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            return "user/signup";
        }

        try {
            userService.signup(request);
        } catch (IllegalArgumentException e) {
            bindingResult.reject("signupFail", e.getMessage());
            return "user/signup";
        }

        return "redirect:/users/login?signup=true";
    }

    /**
     * 로그인 페이지
     * 
     * 실제 로그인 처리는 Spring Security의 formLogin이 담당합니다.
     * - POST /users/login 요청을 Spring Security가 가로채서 처리
     * - 성공: LoginSuccessHandler 실행
     * - 실패: LoginFailureHandler 실행
     */
    @GetMapping("/login")
    public String loginPage(Model model,
                            @RequestParam(required = false) String error,
                            @RequestParam(required = false) String message,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String signup) {
        
        model.addAttribute("loginRequest", new LoginRequest());
        
        // 에러 메시지 처리
        if (error != null && message != null) {
            model.addAttribute("errorMessage", message);
        }
        
        // 로그아웃 성공 메시지
        if (logout != null) {
            model.addAttribute("logoutMessage", "로그아웃 되었습니다.");
        }
        
        // 회원가입 성공 메시지
        if (signup != null) {
            model.addAttribute("signupMessage", "회원가입이 완료되었습니다. 로그인해주세요.");
        }
        
        return "user/login";
    }

    // 로그아웃은 Spring Security가 /users/logout POST 요청을 처리합니다.
    // 별도의 컨트롤러 메서드가 필요하지 않습니다.
}
