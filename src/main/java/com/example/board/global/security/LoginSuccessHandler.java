package com.example.board.global.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 로그인 성공 시 실행되는 핸들러
 * 
 * Spring Security의 AuthenticationSuccessHandler를 구현합니다.
 * 로그인 성공 후의 동작을 커스터마이징할 수 있습니다.
 * 
 * [사용 예시]
 * - 로그인 성공 로그 기록
 * - 마지막 로그인 시간 업데이트
 * - 특정 페이지로 리다이렉트
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                         HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        
        // 인증된 사용자 정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        // 로그인 성공 로그 (실제 운영에서는 Logger 사용 권장)
        System.out.println("[로그인 성공] 사용자: " + userDetails.getDisplayName() 
                + " (ID: " + userDetails.getUserId() + ")");
        
        // 홈 페이지로 리다이렉트
        response.sendRedirect("/");
    }
}

