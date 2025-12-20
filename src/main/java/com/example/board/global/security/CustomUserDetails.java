package com.example.board.global.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.board.domain.user.entity.User;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Spring Security의 UserDetails 구현체
 * 
 * UserDetails는 Spring Security에서 사용자 정보를 담는 인터페이스입니다.
 * 인증(Authentication) 과정에서 사용자 정보를 Spring Security에 전달하는 역할을 합니다.
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    /**
     * 사용자의 권한 목록을 반환합니다.
     * Role enum을 Spring Security의 GrantedAuthority로 변환합니다.
     * "ROLE_" 접두사는 Spring Security의 관례입니다.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Spring Security에서 사용자를 식별하는 고유 값
     * 우리 시스템에서는 loginId를 사용합니다.
     */
    @Override
    public String getUsername() {
        return user.getLoginId();
    }

    /**
     * 계정 만료 여부 (true = 만료되지 않음)
     * 현재는 만료 기능을 사용하지 않으므로 항상 true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부 (true = 잠기지 않음)
     * 현재는 잠금 기능을 사용하지 않으므로 항상 true
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 비밀번호 만료 여부 (true = 만료되지 않음)
     * 현재는 비밀번호 만료 기능을 사용하지 않으므로 항상 true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부 (true = 활성화됨)
     * 현재는 비활성화 기능을 사용하지 않으므로 항상 true
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    // === 편의 메서드 ===

    /**
     * User 엔티티의 ID를 반환합니다.
     * 컨트롤러에서 자주 사용하므로 편의 메서드로 제공합니다.
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * 사용자의 표시 이름을 반환합니다.
     */
    public String getDisplayName() {
        return user.getUsername();
    }
}

