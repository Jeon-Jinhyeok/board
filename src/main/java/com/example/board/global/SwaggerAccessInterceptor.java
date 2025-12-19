package com.example.board.global;

import com.example.board.domain.user.entity.Role;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SwaggerAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        // 세션이 없거나 로그인하지 않은 경우
        if (session == null || session.getAttribute("loginUserId") == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Swagger 접근 권한이 없습니다. 로그인이 필요합니다.");
            return false;
        }

        // Role 확인
        Role role = (Role) session.getAttribute("loginUserRole");
        if (role != Role.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Swagger 접근 권한이 없습니다. ADMIN 권한이 필요합니다.");
            return false;
        }

        return true;
    }
}

