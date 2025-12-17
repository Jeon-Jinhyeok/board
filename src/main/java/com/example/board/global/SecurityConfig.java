package com.example.board.global;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // Spring 설정 클래스
@EnableWebSecurity // 웹 보안 기능 활성화
public class SecurityConfig {
    
    // BCrypt를 스프링 빈으로 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // HTTP 요청에 대한 보안 설정 (필터 체인)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
            .csrf(AbstractHttpConfigurer::disable) // CSRF 보안 비활성화
            .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스 허용
                .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                // 사용자 관련 URL 허용
                .requestMatchers("/", "/users/signup", "/users/login", "/users/logout").permitAll()
                // 게시글 조회는 모두 허용 (GET)
                .requestMatchers("/posts", "/posts/**").permitAll()
                // 그 외 모든 요청도 허용 (세션 기반 인증을 직접 구현하므로)
                .anyRequest().permitAll()
            );
        return http.build();
    }
}
