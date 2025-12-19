package com.example.board.domain.bookmark.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.board.domain.bookmark.entity.Bookmark;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long>{
    
    // 사용자의 북마크 목록 조회
    @Query("SELECT b FROM Bookmark b JOIN FETCH b.post WHERE b.user.id = :userId")
    List<Bookmark> findByUserId(@Param("userId") Long userId);

    // 특정 게시글 북마크 여부 확인
    Optional<Bookmark> findByUserIdAndPostId(Long userId, Long postId);

    // 북마크 존재 여부 확인
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    
    // 사용자의 북마크 삭제
    void deleteByUserIdAndPostId(Long userId, Long postId);


}
