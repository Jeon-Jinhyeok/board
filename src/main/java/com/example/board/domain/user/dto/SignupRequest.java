package com.example.board.domain.user.dto;

import com.example.board.domain.user.entity.Role;
import com.example.board.domain.user.entity.User;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank(message="아이디는 필수입니다.")
    private String loginId;

    @NotBlank(message="비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message="이름은 필수입니다.")
    private String username;

    // DTO -> Entity 변환 메서드
    public User toEntity(String encodedPassword){
        return User.builder()
            .loginId(this.loginId)
            .password(encodedPassword)
            .username(this.username)
            .role(Role.USER)
            .build();
    }
}
