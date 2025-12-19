package com.example.board.domain.comment.entity;

import com.example.board.domain.post.entity.Post;
import com.example.board.domain.user.entity.User;
import com.example.board.global.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="comment_id")
    private Long id;

    @Lob
    @Column(nullable=false)
    private String content;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="parent_id")
    private Comment parent;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="post_id", nullable=false)
    private Post post;

    private boolean isDeleted;

    @Builder
    public Comment(String content, User user, Post post, Comment parent){
        this.content = content;
        this.user = user;
        this.post = post;
        this.parent = parent;
        this.isDeleted = false;
    }

    // 비즈니스 로직

    // 댓글 수정
    public void update(String content){
        this.content = content;
    }

    // 댓글 삭제
    public void delete(){
        this.isDeleted= true;
        this.content= "삭제된 댓글입니다.";
    }

}