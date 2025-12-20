package com.example.board.domain.bookmark.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.board.domain.bookmark.entity.Bookmark;
import com.example.board.domain.bookmark.repository.BookmarkRepository;
import com.example.board.domain.post.dto.PostResponse;
import com.example.board.domain.post.entity.Post;
import com.example.board.domain.post.repository.PostRepository;
import com.example.board.domain.user.entity.User;
import com.example.board.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 북마크 서비스
 * 
 * [수정 사항]
 * - 북마크 추가/삭제 시 Post의 bookmarkCount 업데이트 추가
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    /**
     * 북마크 토글 (있으면 삭제, 없으면 추가)
     * 
     * @return true: 북마크 추가됨, false: 북마크 해제됨
     */
    @Transactional
    public boolean toggleBookmark(Long userId, Long postId) {
        if (bookmarkRepository.existsByUserIdAndPostId(userId, postId)) {
            removeBookmark(userId, postId);
            return false;
        } else {
            addBookmark(userId, postId);
            return true;
        }
    }

    /**
     * 북마크 추가
     */
    @Transactional
    public void addBookmark(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .post(post)
                .build();

        bookmarkRepository.save(bookmark);
        
        // 게시글의 북마크 카운트 증가
        post.increaseBookmarkCount();
    }

    /**
     * 북마크 삭제
     */
    @Transactional
    public void removeBookmark(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        
        bookmarkRepository.deleteByUserIdAndPostId(userId, postId);
        
        // 게시글의 북마크 카운트 감소
        post.decreaseBookmarkCount();
    }

    /**
     * 사용자의 북마크한 게시글 목록 조회
     */
    public List<PostResponse> getBookmarkedPosts(Long userId) {
        List<Bookmark> bookmarks = bookmarkRepository.findByUserId(userId);

        return bookmarks.stream()
                .map(bookmark -> PostResponse.fromList(bookmark.getPost()))
                .toList();
    }

    /**
     * 특정 게시글의 북마크 여부 확인
     */
    public boolean isBookmarked(Long userId, Long postId) {
        if (userId == null) {
            return false;
        }
        return bookmarkRepository.existsByUserIdAndPostId(userId, postId);
    }
}
