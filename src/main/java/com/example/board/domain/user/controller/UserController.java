package com.example.board.domain.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.board.domain.user.dto.SignupRequest;
import com.example.board.domain.user.dto.LoginRequest;
import com.example.board.domain.user.entity.User;
import com.example.board.domain.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/signup")
    public String signupPage(Model model){
        model.addAttribute("signupRequest", new SignupRequest());
        return "user/signup";
    }

    @GetMapping("/login")
    public String loginPage(Model model){
        model.addAttribute("loginRequest", new LoginRequest());
        return "user/login";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupRequest request, BindingResult bindingResult, HttpServletRequest httpRequest){

        if(bindingResult.hasErrors()){
            return "user/signup";
        }

        try{
            userService.signup(request);
        } catch (IllegalArgumentException e){
            bindingResult.reject("signupFail", e.getMessage());
            return "user/signup";
        }

        return "redirect:/users/login"; //성공 시 로그인 페이지로 이동

    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest loginRequest, BindingResult bindingResult, HttpServletRequest httpRequest){

        if(bindingResult.hasErrors()){
            return "user/login";
        }
        try{
            User user = userService.login(loginRequest);

            HttpSession session = httpRequest.getSession(true);

            session.setAttribute("loginUserId", user.getId());
            session.setAttribute("loginUserRole", user.getRole());
        } catch (IllegalArgumentException e){
            bindingResult.reject("loginFail", e.getMessage());
            return "user/login";
        }
        
        return "redirect:/"; // 성공 시 홈으로 이동
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest httpRequest){
        HttpSession session = httpRequest.getSession(false);
        if(session != null){
            session.invalidate();
        }
        return "redirect:/users/login";
    }
}
