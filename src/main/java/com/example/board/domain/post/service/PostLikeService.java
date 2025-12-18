package com.example.board.domain.post.service;

import org.springframework.stereotype.Service;

import com.example.board.domain.post.entity.LikeType;
import com.example.board.domain.post.entity.Post;
import com.example.board.domain.post.entity.PostLike;
import com.example.board.domain.post.repository.PostLikeRepository;
import com.example.board.domain.post.repository.PostRepository;
import com.example.board.domain.user.entity.User;
import com.example.board.domain.user.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public String toggleLike(Long postId, Long userId){
        return toggleReaction(postId, userId, LikeType.LIKE);
    }

    @Transactional
    public String toggleDisLike(Long postId, Long userId){
        return toggleReaction(postId, userId, LikeType.DISLIKE);
    }

    private String toggleReaction(Long postId, Long userId, LikeType likeType){
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return postLikeRepository.findByPostAndUser(post, user)
        .map(existing -> handleExisting(post, existing, likeType))
        .orElseGet(() -> createNew(post, user, likeType));
    }

    private String handleExisting(Post post, PostLike existing, LikeType likeType){
        if(existing.getLikeType() == likeType){     // 같은 타입일 경우
            if(likeType == LikeType.LIKE){
                post.decreaseLikeCount();
            } else {
                post.decreaseDislikeCount();
            }
            postLikeRepository.delete(existing);
            return "cancelled";
        } else { // 다른 타입이였다가 변경
            if(likeType == LikeType.LIKE){
                post.increaseLikeCount();
                post.decreaseDislikeCount();
            } else {
                post.increaseDislikeCount();
                post.decreaseLikeCount();
            }
            existing.changeLikeType(likeType);
            return "changed";
        }
    }

    private String createNew(Post post, User user, LikeType likeType){
        PostLike postLike = PostLike.builder()
            .post(post).user(user).likeType(likeType).build();
        
        postLikeRepository.save(postLike);
        if(likeType == LikeType.LIKE){
            post.increaseLikeCount();
        } else {
            post.increaseDislikeCount();
        }
        return "created";
    }

    public LikeType getUserLikeType(Long postId, Long userId){
        if(userId == null){
            return null;
        }
        Post post = postRepository.findById(postId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        if(post == null || user == null){
            return null;
        }
        return postLikeRepository.findByPostAndUser(post, user)
            .map(PostLike::getLikeType)
            .orElse(null);
    }
}
