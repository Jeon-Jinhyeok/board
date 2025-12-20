package com.example.board.global;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.board.global.security.LoginFailureHandler;
import com.example.board.global.security.LoginSuccessHandler;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security 설정 클래스
 * 
 * [주요 개념]
 * 1. SecurityFilterChain: HTTP 요청에 대한 보안 필터 체인
 * 2. formLogin: 폼 기반 로그인 설정
 * 3. authorizeHttpRequests: URL별 접근 권한 설정
 * 4. PasswordEncoder: 비밀번호 암호화 방식 설정
 * 
 * [인증 흐름]
 * 1. 사용자가 /users/login으로 POST 요청 (loginId, password)
 * 2. Spring Security가 CustomUserDetailsService.loadUserByUsername() 호출
 * 3. 반환된 UserDetails의 password와 입력된 password를 PasswordEncoder로 비교
 * 4. 일치하면 LoginSuccessHandler 실행, 불일치하면 LoginFailureHandler 실행
 * 5. 인증 성공 시 SecurityContext에 Authentication 객체 저장 (세션에 자동 저장)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    /**
     * 비밀번호 암호화에 사용할 PasswordEncoder 빈 등록
     * BCrypt는 단방향 해시 함수로, 같은 비밀번호도 매번 다른 해시값 생성
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP 보안 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 보호 비활성화 (REST API의 경우 보통 비활성화, 폼 기반은 활성화 권장)
            // TODO: 프로덕션에서는 CSRF 활성화 고려
            .csrf(AbstractHttpConfigurer::disable)

            // HTTP Basic 인증 비활성화 (폼 로그인 사용)
            .httpBasic(AbstractHttpConfigurer::disable)

            // 폼 로그인 설정
            .formLogin(form -> form
                // 로그인 페이지 URL (GET 요청)
                .loginPage("/users/login")
                // 로그인 처리 URL (POST 요청) - 이 URL로 폼이 제출됨
                .loginProcessingUrl("/users/login")
                // 폼의 아이디 필드 name 속성
                .usernameParameter("loginId")
                // 폼의 비밀번호 필드 name 속성
                .passwordParameter("password")
                // 로그인 성공 핸들러
                .successHandler(loginSuccessHandler)
                // 로그인 실패 핸들러
                .failureHandler(loginFailureHandler)
                // 로그인 페이지는 누구나 접근 가능
                .permitAll()
            )

            // 로그아웃 설정
            .logout(logout -> logout
                // 로그아웃 처리 URL
                .logoutUrl("/users/logout")
                // 로그아웃 성공 시 이동할 URL
                .logoutSuccessUrl("/users/login?logout=true")
                // 세션 무효화
                .invalidateHttpSession(true)
                // 쿠키 삭제
                .deleteCookies("JSESSIONID")
                // 로그아웃 URL은 누구나 접근 가능
                .permitAll()
            )

            // URL별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스는 누구나 접근 가능
                .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                
                // 인증 관련 페이지는 누구나 접근 가능
                .requestMatchers("/users/signup", "/users/login").permitAll()
                
                // 홈, 게시글 목록/상세는 누구나 조회 가능
                .requestMatchers("/", "/posts", "/posts/{postId}").permitAll()
                
                // API 중 조회는 허용 (좋아요 수 등)
                .requestMatchers("/api/posts/*/counts").permitAll()
                
                // Swagger는 ADMIN 권한 필요
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").hasRole("ADMIN")
                
                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
