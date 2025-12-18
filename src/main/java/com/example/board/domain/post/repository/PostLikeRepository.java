package com.example.board.domain.post.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.board.domain.post.entity.Post;
import com.example.board.domain.post.entity.PostLike;
import com.example.board.domain.user.entity.User;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long>{
    
    Optional<PostLike> findByPostAndUser(Post post, User user); // 특정 게시글과 특정 사용자에 대한 좋아요/싫어요 조회
    boolean existsByPostAndUser(Post post, User user); // 특정 게시글에 특정 사용자가 좋아요/싫어요를 눌렀는지 확인
    void deleteByPostAndUser(Post post, User user); // 특정 게시글의 특정 사용자 좋아요/싫어요 삭제
}
