package com.example.board.domain.post.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.board.domain.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 전체 게시글 최신순 조회
    List<Post> findAllByOrderByCreatedAtDesc();
    
    // 카테고리별 글 조회 (최신순)
    List<Post> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);
    
    // 전체 게시글 페이징 조회 (최신순)
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // 카테고리별 글 페이징 조회 (최신순)
    Page<Post> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);
}
