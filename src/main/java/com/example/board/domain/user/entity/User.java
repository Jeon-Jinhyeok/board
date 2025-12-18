package com.example.board.domain.user.entity;

import com.example.board.global.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="users")
public class User extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;

    @Column(nullable=false, unique=true, length=50)
    private String loginId;
    @Column(nullable=false, length=255)
    private String password;
    @Column(nullable=false, length=30)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Role role;

    @Builder
    public User(String loginId, String password, String username, Role role){
        this.loginId = loginId;
        this.password = password;
        this.username = username;
        this.role = role;
    }
}
