package com.example.board.global;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정 클래스
 * 
 * [참고] Swagger 접근 제어
 * 기존에는 SwaggerAccessInterceptor를 사용했으나,
 * Spring Security 적용 후 SecurityConfig에서 URL 권한 설정으로 처리합니다.
 * 
 * .requestMatchers("/swagger-ui/**").hasRole("ADMIN")
 * 
 * 필요시 이 파일에 추가 WebMVC 설정을 할 수 있습니다:
 * - CORS 설정
 * - 리소스 핸들러 설정
 * - 뷰 컨트롤러 설정
 * - 메시지 컨버터 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    // 필요한 WebMVC 설정을 여기에 추가합니다.
}
