package com.example.board.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.board.domain.user.dto.SignupRequest;
import com.example.board.domain.user.entity.User;
import com.example.board.domain.user.repository.UserRepository;
import com.example.board.domain.user.dto.LoginRequest;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signup(SignupRequest request){

        // 중복 아이디 체크
        if (userRepository.existsByLoginId(request.getLoginId())){
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        // 중복 이름 체크
        if (userRepository.existsByUsername(request.getUsername())){
            throw new IllegalArgumentException("이미 존재하는 이름입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // DB 저장
        return userRepository.save(request.toEntity(encodedPassword)).getId();
    }

    public User login(LoginRequest request){
        User user = userRepository.findByLoginId(request.getLoginId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    // 사용자 조회
    public User findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
