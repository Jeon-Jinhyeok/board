package com.example.board.domain.post.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import com.example.board.domain.post.repository.PostRepository;
import com.example.board.domain.user.repository.UserRepository;
import com.example.board.domain.user.entity.User;
import com.example.board.domain.post.dto.PostCreateRequest;
import com.example.board.domain.post.dto.PostResponse;
import com.example.board.domain.post.entity.Post;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 전체 게시글 목록 조회 (최신순)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(PostResponse::fromList)
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회
    // userId: 현재 로그인한 사용자 ID (비로그인이면 null)
    @Transactional
    public PostResponse getPost(Long postId, Long loginUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        
        Long postWriterId = post.getUser().getId(); // 게시글 작성자 ID

        // 비로그인 사용자이거나, 본인 글이 아닌 경우에만 조회수 증가
        if (loginUserId == null || !loginUserId.equals(postWriterId)) {
            post.increaseViewCount();
        }
        
        return PostResponse.fromDetail(post);
    }

    @Transactional
    public Long write(Long userId, PostCreateRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();

        return postRepository.save(post).getId();
    }
}
