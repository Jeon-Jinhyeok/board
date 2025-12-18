package com.example.board.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.board.domain.user.entity.User;

// User: 조작할 대상. 
// repository.findAll()을 호출하면 자동으롤 SELECT FROM user 쿼리가 나감
// repository.save(entity)를 호출하면 자동으로 INSERT INTO user 쿼리가 나감
// 두번째 파라미터: Long : pk의 타입 -> user엔티티의 PK(id) 타입은 Long이다.

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);
    boolean existsByUsername(String username);
}
