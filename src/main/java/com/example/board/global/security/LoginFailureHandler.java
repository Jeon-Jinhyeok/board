package com.example.board.global.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 로그인 실패 시 실행되는 핸들러
 * 
 * Spring Security의 AuthenticationFailureHandler를 구현합니다.
 * 로그인 실패 원인에 따라 다른 에러 메시지를 전달할 수 있습니다.
 */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                         HttpServletResponse response,
                                         AuthenticationException exception) throws IOException, ServletException {
        
        String errorMessage;
        
        if (exception instanceof UsernameNotFoundException) {
            errorMessage = "존재하지 않는 아이디입니다.";
        } else if (exception instanceof BadCredentialsException) {
            errorMessage = "비밀번호가 일치하지 않습니다.";
        } else {
            errorMessage = "로그인에 실패했습니다.";
        }
        
        // URL 인코딩하여 에러 메시지 전달
        String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        response.sendRedirect("/users/login?error=true&message=" + encodedMessage);
    }
}

