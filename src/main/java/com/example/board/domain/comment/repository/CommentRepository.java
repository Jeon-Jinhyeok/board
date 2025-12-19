package com.example.board.domain.comment.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.board.domain.comment.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>{

    // 게시글의 전체 댓글 수 (대댓글 포함)
    Long countByPostId(Long postId);

    // 최상위 댓글 페이징 조회 (ToOne 관계인 User만 fetch join → 페이징 가능)
    @Query("SELECT c FROM Comment c " +
           "JOIN FETCH c.user " +
           "WHERE c.post.id = :postId AND c.parent IS NULL " +
           "ORDER BY c.createdAt ASC")
    Page<Comment> findParentCommentsByPostIdWithUser(@Param("postId") Long postId, Pageable pageable);

    // 부모 댓글 ID 리스트로 대댓글 조회 (User fetch join)
    @Query("SELECT c FROM Comment c " +
           "JOIN FETCH c.user " +
           "WHERE c.parent.id IN :parentIds " +
           "ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentIdsWithUser(@Param("parentIds") List<Long> parentIds);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post.id = :postId")
    void deleteAllByPostId(@Param("postId") Long postId);
}
