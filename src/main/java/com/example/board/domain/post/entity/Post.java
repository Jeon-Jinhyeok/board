package com.example.board.domain.post.entity;

import com.example.board.domain.user.entity.User;
import com.example.board.global.BaseTimeEntity;
import com.example.board.domain.category.entity.Category;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name="post")
public class Post extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user; // 작성자

    @Column(nullable=false)
    private String title;

    @Lob
    @Column(nullable=false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="category_id", nullable=true)
    private Category category;

    

    @Column(columnDefinition = "bigint default 0", nullable=false)
    private Long viewCount = 0L;
    
    @Column(columnDefinition = "bigint default 0", nullable=false)
    private Long likeCount = 0L;
    
    @Column(columnDefinition = "bigint default 0", nullable=false)
    private Long dislikeCount = 0L;
    
    @Column(columnDefinition = "bigint default 0", nullable=false)
    private Long bookmarkCount = 0L;

    @Builder
    public Post(User user, String title, String content, Category category){
        this.user = user;
        this.title = title;
        this.content = content;
        this.category = category;
        this.viewCount = 0L;
        this.likeCount = 0L;
        this.dislikeCount = 0L;
        this.bookmarkCount = 0L;
    }

    // -- 비즈니스 로직 -- 

    // 게시글 수정
    public void update(String title, String content, Category category){
        this.title = title;
        this.content = content;
        this.category = category;
    }

    // 조회수 증가
    public void increaseViewCount(){
        this.viewCount++;
    }

    // 좋아요 관련 메서드
    public void increaseLikeCount(){
        this.likeCount++;
    }

    public void decreaseLikeCount(){
        this.likeCount = Math.max(0, this.likeCount-1);
    }

    // 싫어요 관련 메서드
    public void increaseDislikeCount(){
        this.dislikeCount++;
    }

    public void decreaseDislikeCount(){
        this.dislikeCount = Math.max(0, this.dislikeCount-1);
    }

    // 북마크 관련 메서드
    public void increaseBookmarkCount(){
        this.bookmarkCount++;
    }

    public void decreaseBookmarkCount(){
        this.bookmarkCount = Math.max(0, this.bookmarkCount - 1);
    }

}
