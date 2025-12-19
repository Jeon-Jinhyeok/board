package com.example.board.domain.post.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import com.example.board.domain.category.entity.Category;
import com.example.board.domain.category.service.CategoryService;
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
    private final CategoryService categoryService;

    // 전체 게시글 목록 조회 (최신순)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(PostResponse::fromList)
                .collect(Collectors.toList());
    }

    // 카테고리별 게시글 목록 조회 (최신순)
    public List<PostResponse> getPostsByCategory(Long categoryId) {
        return postRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId)
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
        
        return PostResponse.fromDetail(post, loginUserId);
    }

    @Transactional
    public Long writePost(Long userId, PostCreateRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 카테고리 처리
        Category category = null;
        if (request.getCategoryId() != null) {
            // 기존 카테고리 선택
            category = categoryService.findById(request.getCategoryId());
        } else if (StringUtils.hasText(request.getNewCategoryName())) {
            // 새 카테고리 생성 (또는 기존 카테고리 재사용)
            category = categoryService.getOrCreateCategory(request.getNewCategoryName().trim());
        }

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .category(category)
                .build();

        return postRepository.save(post).getId();
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 작성자 본인만 삭제 가능
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        postRepository.delete(post);
    }

    public Map<String, Long> getPostLikeCounts(Long postId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        Map<String, Long> likeCounts = Map.of(
            "likeCount", post.getLikeCount(),
            "dislikeCount", post.getDislikeCount()
        );
        return likeCounts;
    }
}