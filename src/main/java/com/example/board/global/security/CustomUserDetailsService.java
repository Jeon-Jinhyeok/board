package com.example.board.global.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.board.domain.user.entity.User;
import com.example.board.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security의 UserDetailsService 구현체
 * 
 * UserDetailsService는 사용자 정보를 로드하는 핵심 인터페이스
 * Spring Security가 로그인 시 이 서비스를 호출하여 사용자 정보를 가져옴
 * 
 * [동작 흐름]
 * 1. 사용자가 로그인 폼에서 아이디/비밀번호 입력
 * 2. Spring Security가 이 서비스의 loadUserByUsername() 호출
 * 3. DB에서 사용자 조회 후 UserDetails 객체 반환
 * 4. Spring Security가 비밀번호 검증 (PasswordEncoder 사용)
 * 5. 인증 성공 시 SecurityContext에 사용자 정보 저장
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 로그인 아이디로 사용자 정보를 조회
     * 
     * @param loginId 사용자가 입력한 로그인 아이디
     * @return UserDetails 객체 (CustomUserDetails)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 아이디입니다: " + loginId));

        return new CustomUserDetails(user);
    }
}

